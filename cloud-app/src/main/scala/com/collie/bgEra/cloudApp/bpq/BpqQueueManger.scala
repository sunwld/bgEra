package com.collie.bgEra.cloudApp.bpq

import com.collie.bgEra.cloudApp.CloudAppContext
import com.collie.bgEra.cloudApp.redisCache.RedisService
import com.collie.bgEra.cloudApp.utils.ContextHolder
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
    manger
  }
}
