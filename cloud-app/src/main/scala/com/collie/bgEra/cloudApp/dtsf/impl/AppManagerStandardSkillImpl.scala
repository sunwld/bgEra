package com.collie.bgEra.cloudApp.dtsf.impl

import com.collie.bgEra.cloudApp.appm.{AppManagerStandardSkill, ClusterInfo}
import com.collie.bgEra.cloudApp.dtsf.ShardingManager
import com.collie.bgEra.cloudApp.dtsf.mapper.TaskMapper
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scala.collection.JavaConversions._

@Component("appManagerStandardSkill")
class AppManagerStandardSkillImpl extends AppManagerStandardSkill {

  @Autowired
  private val shardingManager: ShardingManager = null

  private val logger: Logger = LoggerFactory.getLogger("dtsf")

  override def suspend(clusterInfo: ClusterInfo): Unit = {
    println(s"suspend $clusterInfo")
  }

  override def resume(clusterInfo: ClusterInfo): Unit = {
    println(s"resume $clusterInfo")
  }

  override def close(clusterInfo: ClusterInfo): Unit = {
    println(s"close $clusterInfo")
  }

  override def reconstruction(clusterInfo: ClusterInfo): Unit = {
    println(s"reconstruction $clusterInfo")
  }

  override def reallocation(clusterInfo: ClusterInfo): Unit = {
    logger.info(s"reallocation $clusterInfo")
    shardingManager.saveZksessionInfo(clusterInfo)
    shardingManager.reshardTargets(clusterInfo.clusterVotids)
  }
}
