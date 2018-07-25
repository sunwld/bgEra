package com.collie.bgEra.cloudApp.dtsf.impl

import java.util

import com.collie.bgEra.cloudApp.appm.ClusterInfo
import com.collie.bgEra.cloudApp.dtsf.ShardingManager
import com.collie.bgEra.cloudApp.dtsf.bean.{ShardingTarget, TargetInfo, ZkSessionInfo}
import com.collie.bgEra.cloudApp.dtsf.mapper.TaskMapper
import com.collie.bgEra.cloudApp.redisCache.RedisService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._

@Component
class ShardingManagerImpl extends ShardingManager{
  @Autowired
  private val taskMapper: TaskMapper = null

  override def generateCurrentInstanceId: Long = ???

  override def queryShardingInfoByInstaceId(instId: Long): Unit = ???

  override def reShardTargetsForNewer(): Unit = ???

  override def reShardTargetsAfterLeaver(): Unit = ???

  override def  reshardTargets(zkSessionIds: mutable.Seq[String]) = {
    val dtsfTargList: mutable.Seq[TargetInfo] = taskMapper.qryAllTartInfo()
    val sharedTargetInfo: util.List[ShardingTarget] = new util.ArrayList[ShardingTarget]()
    val nodeCount = zkSessionIds.size
    val shardMap = new mutable.HashMap[String,mutable.Seq[TargetInfo]]()
    var index: Int = -1
    var list : Seq[TargetInfo] = null
    dtsfTargList.foreach(targetInfo => {
      index = targetInfo.shardingValue % nodeCount
      list = shardMap.getOrElseUpdate(zkSessionIds(index),ListBuffer[TargetInfo]())
      sharedTargetInfo.add(ShardingTarget(zkSessionIds(index),targetInfo.name))
      list.asInstanceOf[ListBuffer[TargetInfo]].append(targetInfo)
    })
    taskMapper.insertTargetShardingMap(sharedTargetInfo)
    shardMap
  }

  override def saveZksessionInfo(clusterInfo: ClusterInfo): Unit = {
    val votids: mutable.Seq[String] = clusterInfo.clusterVotids
    val clusterInfoList: mutable.Seq[ZkSessionInfo] = votids.map(nodeSessionId => {
      if(nodeSessionId.equals(clusterInfo.currentVotid)){
        ZkSessionInfo(nodeSessionId, true)
      }else{
        ZkSessionInfo(nodeSessionId,false)
      }
    })
    taskMapper.insertZkSessionInfo(clusterInfoList)
  }

  /**
    * 清空dtsf 的redis 缓存
    */
  override def flushCache(): Unit = {
    taskMapper.flushDtsfRedisCache()
  }

  /**
    * 初始化 dtsf的redis缓存
    * @param clusterInfo
    */
  override def initRedisCache(clusterInfo: ClusterInfo): Unit = {
  }

  /**
    *释放 datasorece、ssh2、jmx等资源池
    */
  override def flustResource(): Unit = ???
}
