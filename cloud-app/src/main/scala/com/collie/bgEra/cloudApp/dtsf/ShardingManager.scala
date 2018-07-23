package com.collie.bgEra.cloudApp.dtsf

import com.collie.bgEra.cloudApp.appm.ClusterInfo
import com.collie.bgEra.cloudApp.dtsf.bean.TargetInfo

import scala.collection.mutable

trait ShardingManager {

  def flushCache(): Unit

  def initRedisCache(clusterInfo: ClusterInfo): Unit

  def generateCurrentInstanceId: Long

  def queryShardingInfoByInstaceId(instId: Long): Unit

  def reShardTargetsForNewer(): Unit

  def reShardTargetsAfterLeaver(): Unit

  def saveZksessionInfo(clusterInfo: ClusterInfo)

  def reshardTargets(zkSessionIds: mutable.Seq[String]): mutable.HashMap[String,mutable.Seq[TargetInfo]]
}
