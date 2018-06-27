package com.collie.bgEra.cloudApp.dtsf

import com.collie.bgEra.cloudApp.dtsf.bean.ShardingInfo

trait ShardingManager {

  def generateCurrentInstanceId: Long

  def queryShardingInfoByInstaceId(instId: Long): ShardingInfo

  def reShardTargetsForNewer(): Unit

  def reShardTargetsAfterLeaver(): Unit

  def reshardTargets(): Unit
}
