package com.collie.bgEra.cloudApp.dtsf.impl

import com.collie.bgEra.cloudApp.dtsf.{TaskManager, TaskSchedule, WorkUnitRunable}
import com.collie.bgEra.cloudApp.dtsf.bean.{TaskInfo, _}
import com.collie.bgEra.cloudApp.dtsf.mapper.TaskMapper
import com.collie.bgEra.cloudApp.redisCache.bean.ZSetItemBean
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Component
import java.{util => ju}

import com.collie.bgEra.cloudApp.CloudAppContext
import com.collie.bgEra.cloudApp.redisCache.HsetDelItem
import com.collie.bgEra.cloudApp.utils.ContextHolder
import com.collie.bgEra.commons.util.DateUtils
import org.apache.ibatis.session.{SqlSession, SqlSessionFactory}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._
import scala.collection.mutable


@Component
class TaskManagerImpl extends TaskManager {
  private val logger: Logger = LoggerFactory.getLogger("dtsf")


  private val lock: Object = new Object()
  private val taskLockMap: ju.Map[String,Object] = new ju.HashMap()

  @Autowired
  val taskMapper: TaskMapper = null

  @Autowired
  val context: CloudAppContext = null

  override def getPreparedTaskList(zkSessionId: String): ju.List[ZSetItemBean] = {
    var preparedTask: ju.List[ZSetItemBean] = null
    //根据zkSessionid获取此zksessionid 分配到的targetList
    val targetSharding: ju.List[String] = taskMapper.getTargetShardingBySessionId(zkSessionId)
    if (targetSharding != null && !targetSharding.isEmpty()) {
      //查询出可被执行的taskId
      val getTaskListBeginStamp = System.currentTimeMillis()
      preparedTask = taskMapper.qryPerpredTaskListByTargets(zkSessionId, targetSharding, System.currentTimeMillis())
      val getTaskListLong = System.currentTimeMillis() - getTaskListBeginStamp
      logger.debug(s"qryPerpredTaskListByTargets elapse $getTaskListLong ms.")
    }
    preparedTask
  }

  @Autowired
  @Qualifier("bgEra_dtsf_SqlSessionFactory")
  private val factory: SqlSessionFactory = null


  override def runTask(taskInfo: TaskInfo): Unit = {
    //用于记录此次task执行过程中，失败的workUnit的数量
    var exceptionUnitSize = 0
    val nextTimeSched: TaskSchedule = ContextHolder.getBean(taskInfo.taskSchedulerBean)

    var session: SqlSession = null
    try {
      session = factory.openSession(false)

      var taskLock: Object = taskLockMap.get(taskInfo.taskId)
      if (taskLock == null) {
        lock.synchronized {
          taskLock = taskLockMap.get(taskInfo.taskId)
          if (taskLock == null){
            taskLock = new Object()
            taskLockMap.put(taskInfo.taskId,taskLock)
          }
        }
      }

      val lockTimeStamp: Long = System.currentTimeMillis()
      taskLock.synchronized {
        //如果当前task是running状态，则修改task状态，并跳过此次执行
        if ("RUNNING".equals(taskInfo.status)) {
          logger.info(s"task status is allready running,will skip this task:$taskInfo")
          return
        } else if (taskInfo.nextTime.after(new ju.Date()) && "WAITING".equals(taskInfo.status)) {
          logger.info(s"this task is allready executed,will execute at nexttime:$taskInfo")
          return
        }

        //修改task状态为running，并保存
        taskInfo.thisTime = new ju.Date()
        taskInfo.status = "RUNNING"
        taskMapper.updateTaskInfo(taskInfo, session)
      }
      val lockTimeLong = System.currentTimeMillis() - lockTimeStamp
      logger.debug(s"run task check elapse $lockTimeLong ms,task:$taskInfo")

      //遍历task中的workUnit，并执行
      val workUnitIds = taskInfo.workUnitList
      var unitResult: WorkUnitResult = null
      workUnitIds.foreach(unitId => {
        //根据 targetId, taskName,unitName查询出对应的 workUnit对象
        val unit = taskMapper.qryWorkUnitInfoById(unitId, session)
        //修改workUnit状态为running，并保存
        unit.thisTime = new ju.Date()
        unit.status = "RUNNING"
        taskMapper.updateWorkUnitInfo(unit, session)

        //执行workunit
        val invokWorkUnitTimeStamp: Long = System.currentTimeMillis()
        unitResult = invokWorkUnit(unit)
        val invokWorkUnitLong: Long = System.currentTimeMillis() - invokWorkUnitTimeStamp
        logger.debug(s"invokWorkUnitLong elapse $invokWorkUnitLong ms:$unit")

        //执行完workUnit之后，修改workUnit状态并保存
        unit.status = "WAITING"
        unit.thisTime = null
        unit.errors = unitResult.result match {
          case WorkUnitResult.EXCEPTION => {
            exceptionUnitSize += 1
            unit.errors + 1
          }
          case _ => 0
        }
        taskMapper.updateWorkUnitInfo(unit, session)
      })

    } catch {
      case e: Exception => {
        try {
          //task执行出现异常时，记录异常信息
          logger.error("task failed and log error, query dtf_errorlog for detail!", e)
          taskMapper.saveDtfErrorLog(TaskErrorBean(new ju.Date(), taskInfo.taskName, taskInfo.targetId,
            null, context.appmClusterInfo.currentVotid, e.getMessage), session)
        } catch {
          //如果记录异常信息出现异常，则直接打印异常信息
          case e2: Exception => logger.error("task failed and log error failed too!", e2)
        }
      }
    } finally {
      //确保，无论task执行成功还是失败，都可以修改和保存task状态
      try {
        taskInfo.status = "WAITING"
        taskInfo.nextTime = nextTimeSched.getNextRunTime(new ju.Date())
        taskInfo.thisTime = null
        taskInfo.errors = exceptionUnitSize match {
          case 0 => 0
          case _ => taskInfo.errors + exceptionUnitSize
        }
        taskMapper.updateTaskInfo(taskInfo, session)
      } catch {
        case e: Exception => {
          e.printStackTrace()
          try {
            //task执行出现异常时，记录异常信息
            taskMapper.saveDtfErrorLog(TaskErrorBean(new ju.Date(), taskInfo.taskName, taskInfo.targetId,
              null, context.appmClusterInfo.currentVotid, e.getMessage), session)
            logger.error("task failed and log error, query dtf_errorlog for detail!", e)
          } catch {
            //如果记录异常信息出现异常，则直接打印异常信息
            case e2: Exception => logger.error("task failed and log error failed too!", e2)
          }
        }
      } finally {
        if (session != null) {
          session.commit()
          session.close()
        }
        //将task重新放入到缓存中
        taskMapper.giveBackTaskZsetList(context.appmClusterInfo.currentVotid,
          ju.Arrays.asList(ZSetItemBean(taskInfo.taskId, taskInfo.nextTime.getTime())))

      }

    }
  }

  override def invokWorkUnit(workUnit: WorkUnitInfo): WorkUnitResult = {
    var unitResult = WorkUnitResult.SUCCESS
    var ex: Exception = null;
    var msg = ""
    try {
      val bean = ContextHolder.getBean(workUnit.springBeanName).asInstanceOf[WorkUnitRunable]
      bean.runWork(workUnit)
    } catch {
      case e: Exception => {
        logger.error(s"run workunit ${workUnit} failed !", e)
        ex = e
        unitResult = WorkUnitResult.EXCEPTION
      }
    }

    WorkUnitResult(unitResult, msg, ex)
  }

  override def finishTask(taskResult: TaskResult, taskScheBeanNm: String): Unit = ???


}
