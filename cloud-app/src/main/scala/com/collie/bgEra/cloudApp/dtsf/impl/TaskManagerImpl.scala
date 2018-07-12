package com.collie.bgEra.cloudApp.dtsf.impl

import com.collie.bgEra.cloudApp.dtsf.{TaskManager, TaskSchedule}
import com.collie.bgEra.cloudApp.dtsf.bean._
import com.collie.bgEra.cloudApp.dtsf.mapper.TaskMapper
import com.collie.bgEra.cloudApp.redisCache.bean.ZSetItemBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.{util => ju}

import com.collie.bgEra.cloudApp.CloudAppContext
import com.collie.bgEra.cloudApp.redisCache.HsetDelItem
import com.collie.bgEra.cloudApp.utils.ContextHolder
import com.collie.bgEra.commons.util.DateUtils
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._


@Component
class TaskManagerImpl extends TaskManager{
  var runCount = 0
  var giveBackCount = 0
  private val logger: Logger = LoggerFactory.getLogger("dtsf")

  @Autowired
  val taskMapper: TaskMapper = null

  @Autowired
  val context: CloudAppContext = null

  override def getPreparedTaskList(zkSessionId: String): ju.List[TaskInfo] = {
    val taskList: ju.List[TaskInfo] = new ju.ArrayList[TaskInfo]()
    //根据zkSessionid获取此zksessionid 分配到的targetList
    val targetSharding: ju.List[String] = taskMapper.getTargetShardingBySessionId(zkSessionId)
    if(targetSharding != null && !targetSharding.isEmpty()){
      //查询出可被执行的taskId
      val preparedTask: ju.List[ZSetItemBean] = taskMapper.qryPerpredTaskListByTargets(zkSessionId, targetSharding, System.currentTimeMillis())

      //根据taskId,找到对应的TarkInfo对象
      preparedTask.foreach(task => {
        taskList.add(taskMapper.qryTaskInfoById(task.id))
      })
    }
    taskList
  }

  override def runTask(taskInfo: TaskInfo): Unit = {

    //用于记录此次task执行过程中，失败的workUnit的数量
    var exceptionUnitSize = 0
    val nextTimeSched: TaskSchedule = ContextHolder.getBean(taskInfo.taskSchedulerBean)
    this.synchronized{
      runCount+=1
    }
    try {
      //如果当前task是running状态，则修改task状态，并跳过此次执行
      if("RUNNING".equals(taskInfo.status)){
        logger.info("RUNNING and skip",taskInfo)
        return
      }else if(taskInfo.nextTime.after(new ju.Date()) && "WAITING".equals(taskInfo.status)){
        logger.info("time and skip",taskInfo)
        return
      }

      //修改task状态为running，并保存
      taskInfo.thisTime = new ju.Date()
      taskInfo.status = "RUNNING"
      taskMapper.updateTaskInfo(taskInfo)

      //遍历task中的workUnit，并执行
      val workUnitNames = taskInfo.workUnitList
      var unitResult: WorkUnitResult = null
      workUnitNames.foreach(unitName => {
        //根据 targetId, taskName,unitName查询出对应的 workUnit对象
        val unit = taskMapper.qryWorkUnitInfoById(s"${taskInfo.targetId}||${taskInfo.taskName}||${unitName}")
        //修改workUnit状态为running，并保存
        unit.thisTime = new ju.Date()
        unit.status = "RUNNING"
        taskMapper.updateWorkUnitInfo(unit)

        //执行workunit
        unitResult = invokWorkUnit(unit)

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
        taskMapper.updateWorkUnitInfo(unit)
      })

    } catch {
      case e: Exception => {
        try {
          //task执行出现异常时，记录异常信息
          taskMapper.saveDtfErrorLog(TaskErrorBean(new ju.Date(), taskInfo.taskName, taskInfo.targetId,
            null, context.appmClusterInfo.currentVotid, e.getMessage))
          logger.error("task failed and log error, query dtf_errorlog for detail!",e)
        } catch {
          //如果记录异常信息出现异常，则直接打印异常信息
          case e2: Exception => logger.error("task failed and log error failed too!",e2)
        }
      }
    } finally {
      //确保，无论task执行成功还是失败，都可以修改和保存task状态
      try {
        taskInfo.status = "WAITING"
        taskInfo.nextTime = nextTimeSched.getNextRunTime(new ju.Date())
        taskInfo.errors = exceptionUnitSize match {
          case 0 => 0
          case _ => taskInfo.errors + exceptionUnitSize
        }
        taskMapper.updateTaskInfo(taskInfo)
      } catch {
        case e: Exception => {
          e.printStackTrace()
          try {
            //task执行出现异常时，记录异常信息
            taskMapper.saveDtfErrorLog(TaskErrorBean(new ju.Date(), taskInfo.taskName, taskInfo.targetId,
              null, context.appmClusterInfo.currentVotid, e.getMessage))
            logger.error("task failed and log error, query dtf_errorlog for detail!",e)
          } catch {
            //如果记录异常信息出现异常，则直接打印异常信息
            case e2: Exception => logger.error("task failed and log error failed too!",e2)
          }
        }
      } finally {
        this.synchronized{
          giveBackCount+=1
        }
        //将task重新放入到缓存中
        taskMapper.giveBackTaskZsetList(context.appmClusterInfo.currentVotid,
          ju.Arrays.asList(ZSetItemBean(taskInfo.targetId + "||" + taskInfo.taskName,taskInfo.nextTime.getTime())))
      }

    }
  }

  override def invokWorkUnit(workUnit: WorkUnitInfo): WorkUnitResult = {
//    println(workUnit)
    WorkUnitResult(WorkUnitResult.SUCCESS,"",null)
  }

  override def finishTask(taskResult: TaskResult, taskScheBeanNm: String): Unit = ???



}
