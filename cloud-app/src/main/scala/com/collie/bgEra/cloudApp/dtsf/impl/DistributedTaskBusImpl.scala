package com.collie.bgEra.cloudApp.dtsf.impl

import java.io.IOException
import java.util
import java.util.Date

import com.collie.bgEra.cloudApp.CloudAppContext
import com.collie.bgEra.cloudApp.dtsf.bean.TaskInfo
import com.collie.bgEra.cloudApp.dtsf.mapper.TaskMapper
import com.collie.bgEra.cloudApp.dtsf.{DistributedTaskBus, TaskManager}
import com.collie.bgEra.cloudApp.redisCache.bean.ZSetItemBean
import javax.xml.parsers.ParserConfigurationException
import org.quartz.impl.StdSchedulerFactory
import org.quartz._
import org.quartz.impl.triggers.SimpleTriggerImpl
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component
import org.xml.sax.SAXException
import org.quartz.Scheduler

import scala.collection.JavaConversions._

@Component("distributedTaskBus")
@EnableScheduling
class DistributedTaskBusImpl extends DistributedTaskBus {
    private val logger: Logger = LoggerFactory.getLogger("dtsf")
    @Autowired
    private val context: CloudAppContext = null
    @Autowired
    private val taskManager: TaskManagerImpl = null
    @Autowired
    private val mainScheduler: Scheduler = null
    @Autowired
    private val taskMapper:TaskMapper = null

    override def runBus(): Unit = {
        val taskList: util.List[ZSetItemBean] = taskManager.getPreparedTaskList(context.appmClusterInfo.currentVotid)
        if(taskList == null || taskList.size() == 0){
          return
        }

        logger.info("scanned " + taskList.size() + " task")
        taskList.foreach(taskZset => {
            val taskInfo = taskMapper.qryTaskInfoById(taskZset.getId)
            val scheduler: Scheduler = getScheduler(taskInfo.taskThreadPoolName)
            fillScheduler(scheduler,taskInfo)
            if (!scheduler.isStarted()){
                scheduler.start()
            }
        })
    }

    override def startScheduler(): Unit = {
        try
            if (mainScheduler != null && mainScheduler.isInStandbyMode){
                mainScheduler.start()
            }
        catch {
            case e: Exception =>
                logger.error("run start main scheduler failed:", e)
        }
    }

    override def stopScheduler(): Unit = {
        try
            if (mainScheduler != null && !mainScheduler.isShutdown) {
                mainScheduler.standby()
            }
        catch {
            case e: Exception =>
                logger.error("run stop main scheduler failed:", e)
        }
    }

    /**
      * 根据task相关信息，填充此Scheduler对象
      */
    @throws[SchedulerException]
    private def fillScheduler(scheduler: Scheduler, task: TaskInfo): Unit = {
        val name = task.targetId + "||" + task.taskName
        val group = "defaultGroup"
        //如果当前scheduler不存在此job，则填充此job
        val jobkey = new JobKey(name + "_job", group)
        if (scheduler.getJobDetail(jobkey) == null) {
            val jobDetail = JobBuilder.newJob(classOf[QuartzJob]).withIdentity(jobkey).build()
            //将传入的JobBean对象放到JobDataMap对象中，这样当此job运行时，可以获取JobBean对象
            val jobDataMap = jobDetail.getJobDataMap
            jobDataMap.put("task", task)
            jobDataMap.put("taskManager", taskManager)
            val trigger = TriggerBuilder.newTrigger.withIdentity(name + "_Trigger", group).build().asInstanceOf[SimpleTriggerImpl]
            //只执行一次，并且立刻执行
            trigger.setStartTime(new Date())
            trigger.setRepeatInterval(20)
            trigger.setRepeatCount(0)
            scheduler.scheduleJob(jobDetail, trigger)
        }else {
            logger.info("task : " + jobkey + "is already running.")
        }
    }

    /**
      * 根据传入的自定义的schedulerId，获取对应的Scheduler对象
      *
      * @return
      * @throws SchedulerException
      * @throws ParserConfigurationException
      * @throws SAXException
      * @throws IOException
      */
    @throws[SchedulerException]
    @throws[ParserConfigurationException]
    @throws[SAXException]
    @throws[IOException]
    private def getScheduler(threadPool: String) = {
        val sf = new StdSchedulerFactory()

        var scheduler = sf.getScheduler(context.getSchedulerNameByThreadPool(threadPool))
        //如果此scheduler还没有被实例化，则根据对应的配置文件，实例化此scheduler
        if (scheduler == null) {
            sf.initialize(context.getQuartzSchedulerPorp(threadPool))
            scheduler = sf.getScheduler()
        }
        scheduler
    }

}
