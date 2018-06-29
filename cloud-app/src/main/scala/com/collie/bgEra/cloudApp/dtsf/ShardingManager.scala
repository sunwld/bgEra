package com.collie.bgEra.cloudApp.dtsf

import com.collie.bgEra.cloudApp.dtsf.bean.{ShardingInfo, TargetInfo}

import scala.collection.mutable

trait ShardingManager {

  def generateCurrentInstanceId: Long

  def queryShardingInfoByInstaceId(instId: Long): ShardingInfo

  def reShardTargetsForNewer(): Unit

  def reShardTargetsAfterLeaver(): Unit

  def reshardTargets(zkSessionIds: List[String], dtsfTargList: List[TargetInfo]): mutable.HashMap[String,List[TargetInfo]]
}
