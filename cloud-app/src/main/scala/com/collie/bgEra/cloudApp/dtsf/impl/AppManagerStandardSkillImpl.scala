package com.collie.bgEra.cloudApp.dtsf.impl

import com.collie.bgEra.cloudApp.CloudAppContext
import com.collie.bgEra.cloudApp.appm.{AppManagerStandardSkill, ClusterInfo}
import com.collie.bgEra.cloudApp.dtsf.{ShardingManager, TaskManager}
import com.collie.bgEra.cloudApp.dtsf.mapper.TaskMapper
import org.quartz.Scheduler
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._

@Component("appManagerStandardSkill")
class AppManagerStandardSkillImpl extends AppManagerStandardSkill {

  @Autowired
  private val cloudAppContext: CloudAppContext = null
  @Autowired
  private val shardingManager: ShardingManager = null
  @Autowired
  private val taskBus: DistributedTaskBusImpl = null

  private val logger: Logger = LoggerFactory.getLogger("dtsf")

  override def suspend(clusterInfo: ClusterInfo): Unit = {
    cloudAppContext.appmClusterInfo = clusterInfo
    taskBus.stopScheduler()
    println(s"suspend $clusterInfo")
  }

  override def resume(clusterInfo: ClusterInfo): Unit = {
    cloudAppContext.appmClusterInfo = clusterInfo
    taskBus.startScheduler()
    println(s"resume $clusterInfo")
  }

  override def close(clusterInfo: ClusterInfo): Unit = {
    cloudAppContext.appmClusterInfo = clusterInfo
    println(s"close $clusterInfo")
  }

  override def reconstruction(clusterInfo: ClusterInfo): Unit = {
    cloudAppContext.appmClusterInfo = clusterInfo
    println(s"reconstruction $clusterInfo")
  }

  override def reallocation(clusterInfo: ClusterInfo): Unit = {
    logger.info(s"reallocation $clusterInfo")
    cloudAppContext.appmClusterInfo = clusterInfo
    shardingManager.flushCache()
    shardingManager.saveZksessionInfo(clusterInfo)
    shardingManager.reshardTargets(clusterInfo.clusterVotids)
  }
}
