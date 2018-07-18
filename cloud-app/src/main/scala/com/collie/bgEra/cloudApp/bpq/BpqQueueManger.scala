package com.collie.bgEra.cloudApp.bpq

import java.util
import java.util.Date

import com.collie.bgEra.cloudApp.CloudAppContext
import com.collie.bgEra.cloudApp.dtsf.impl.QuartzJob
import com.collie.bgEra.cloudApp.redisCache.RedisService
import org.apache.ibatis.session.SqlSessionFactory
import org.quartz.impl.triggers.SimpleTriggerImpl
import org.quartz.{JobBuilder, JobKey, Scheduler, TriggerBuilder}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Component

@Component
class BpqQueueManger {
  private val queueKeyPrefix = "bgEra.cloudApp.bpq"

  @Autowired
  private val redisService: RedisService = null
  @Autowired
  private val context: CloudAppContext = null

  private val queueInfoMap: util.Map[String,Int] = new util.HashMap()

  private val logger: Logger = LoggerFactory.getLogger("bpq")

  def pushItemToQueue(queueId:String, queueItem: QueueItem, maxLength: Int = 5000): Unit ={
    savaQueueInfo(queueId,maxLength)
    val redisKey = s"${queueKeyPrefix}.${context.getAppId()}_${queueId}"
    if(redisService.listSize(redisKey) > getQueueMaxLength(queueId)){
      this.synchronized {
        this.wait()
      }
    }else{
      redisService.listRpush(redisKey,queueItem)
    }
  }

  def unshiftItemToQueue(queueId:String, queueItem: QueueItem): Unit ={
    val redisKey = s"${queueKeyPrefix}.${context.getAppId()}_${queueId}"
    redisService.listLpush(redisKey,queueItem)
  }

  def popItemFromQueue(queueId:String): QueueItem={
    val redisKey = s"${queueKeyPrefix}.${context.getAppId()}_${queueId}"
    val item = redisService.listLpop(redisKey).asInstanceOf[QueueItem]
    if(redisService.listSize(redisKey) < getQueueMaxLength(queueId) * 0.8){
      this.synchronized{
        this.notifyAll()
      }
    }
    item
  }

  def getQueueSize(queueId:String): Long ={
    val redisKey = s"${queueKeyPrefix}.${context.getAppId()}_${queueId}"
    redisService.listSize(redisKey)
  }

  private def savaQueueInfo(queueId:String,maxLength: Integer): Unit ={
    if(!queueInfoMap.containsKey(queueId)){
      queueInfoMap.synchronized{
        if(!queueInfoMap.containsKey(queueId)){
          queueInfoMap.put(queueId,maxLength)
        }
      }
    }
  }

  private def getQueueMaxLength(queueId:String): Int ={
    queueInfoMap.get(queueId)
  }
}
