package com.collie.bgEra.cloudApp.dtsf.impl

import com.collie.bgEra.cloudApp.dtsf.ShardingManager
import com.collie.bgEra.cloudApp.dtsf.bean.{ShardingInfo, TargetInfo}
import org.springframework.stereotype.Component

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

@Component
class ShardingManagerImpl extends ShardingManager{
  override def generateCurrentInstanceId: Long = ???

  override def queryShardingInfoByInstaceId(instId: Long): ShardingInfo = ???

  override def reShardTargetsForNewer(): Unit = ???

  override def reShardTargetsAfterLeaver(): Unit = ???

  override def reshardTargets(zkSessionIds: List[String], dtsfTargList: mutable.Seq[TargetInfo]) = {
    val nodeCount = zkSessionIds.size
    val shardMap = new mutable.HashMap[String,mutable.Seq[TargetInfo]]()
    var index: Int = -1
    var list : Seq[TargetInfo] = null
    dtsfTargList.foreach(targetInfo => {
      index = targetInfo.shardingValue % nodeCount
      list = shardMap.getOrElseUpdate(zkSessionIds(index),ListBuffer[TargetInfo]())
      list.asInstanceOf[ListBuffer[TargetInfo]].append(targetInfo)
    })
    shardMap
  }
}
