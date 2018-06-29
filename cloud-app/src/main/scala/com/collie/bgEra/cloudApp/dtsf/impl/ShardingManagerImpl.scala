package com.collie.bgEra.cloudApp.dtsf.impl

import com.collie.bgEra.cloudApp.dtsf.ShardingManager
import com.collie.bgEra.cloudApp.dtsf.bean.{ShardingInfo, TargetInfo}
import org.springframework.stereotype.Component

import scala.collection.mutable

@Component
class ShardingManagerImpl extends ShardingManager{
  override def generateCurrentInstanceId: Long = ???

  override def queryShardingInfoByInstaceId(instId: Long): ShardingInfo = ???

  override def reShardTargetsForNewer(): Unit = ???

  override def reShardTargetsAfterLeaver(): Unit = ???

  override def reshardTargets(zkSessionIds: List[String], dtsfTargList: List[TargetInfo]) = {
    val nodeCount = zkSessionIds.size
    val shardMap = new mutable.HashMap[String,List[TargetInfo]]()
    var index: Int = -1
    dtsfTargList.foreach(targetInfo => {
      index = targetInfo.shardingValue % nodeCount
      val list: List[TargetInfo] = shardMap.getOrElse(zkSessionIds(index), List[TargetInfo]())
      list :+ targetInfo
    })
    shardMap
  }
}
