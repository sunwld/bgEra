package com.collie.bgEra.cloudApp.bpq

import java.util.Date

import com.collie.bgEra.cloudApp.CloudAppContext
import com.collie.bgEra.cloudApp.redisCache.RedisService
import com.collie.bgEra.cloudApp.utils.ContextHolder
import org.quartz.impl.triggers.SimpleTriggerImpl
import org.quartz.{JobBuilder, JobKey, Scheduler, TriggerBuilder}
import org.slf4j.{Logger, LoggerFactory}


class BpqQueueManger private(val appId: String, val queueId: String, val queueMaxLength: Int = 2000) {
  private val redisKey = s"bgEra.cloudApp.bpq.${appId}_${queueId}"
  private var redisService: RedisService = null
  private val logger: Logger = LoggerFactory.getLogger("bpq")

  def pushItemToQueue(queueItem: QueueItem): Unit ={
    if(redisService.listSize(redisKey) > queueMaxLength){
      this.synchronized {
        this.wait()
      }
    }else{
      redisService.listRpush(redisKey,queueItem)
    }
  }

  def unshiftItemToQueue(queueItem: QueueItem): Unit ={
    redisService.listLpush(redisKey,queueItem)
  }

  def popItemFromQueue(): QueueItem={
    val item = redisService.listLpop(redisKey).asInstanceOf[QueueItem]
    if(redisService.listSize(redisKey) < queueMaxLength * 0.9){
      this.synchronized{
        this.notifyAll()
      }
    }
    item
  }

  def getQueueSize(): Long ={
    redisService.listSize(redisKey)
  }
}

object BpqQueueManger{
  def apply(queueId: String, queueMaxLength: Int = 2000): BpqQueueManger ={
    val appId = ContextHolder.getBean(classOf[CloudAppContext]).getAppId()
    val redisService: RedisService =ContextHolder.getBean(classOf[RedisService])
    val manger = new BpqQueueManger(appId,queueId,queueMaxLength)
    manger.redisService = redisService

    val group = s"${queueId}Group"
    //如果当前scheduler不存在此job，则填充此job
    val jobkey = new JobKey(queueId + "_job", group)
    val scheduler = ContextHolder.getBean("bpqQueueScheduler").asInstanceOf[Scheduler]
    if (scheduler.getJobDetail(jobkey) == null) {
      val jobDetail = JobBuilder.newJob(classOf[BpqSqlQueueBus]).withIdentity(jobkey).build()
      //将传入的JobBean对象放到JobDataMap对象中，这样当此job运行时，可以获取JobBean对象
      val jobDataMap = jobDetail.getJobDataMap
      jobDataMap.put("manager", manger)
      val trigger = TriggerBuilder.newTrigger().withIdentity(queueId + "_Trigger", group).build().asInstanceOf[SimpleTriggerImpl]
      //只执行一次，并且立刻执行
      trigger.setStartTime(new Date())
      trigger.setRepeatInterval(1000)
      trigger.setRepeatCount(-1)
      scheduler.scheduleJob(jobDetail, trigger)
    }else {
      manger.logger.info(s"BPQ queue $queueId already exists!")
    }
    manger
  }
}
