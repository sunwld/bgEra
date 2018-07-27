package com.collie.bgEra.cloudApp.dtsf.impl

import com.collie.bgEra.cloudApp.appm.{AppManagerStandardSkill, ClusterInfo}
import com.collie.bgEra.cloudApp.context.CloudAppContext
import com.collie.bgEra.cloudApp.dtsf.{ShardingManager, TaskManager}
import org.quartz.Scheduler
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._

@Component("appManagerStandardSkill")
class AppManagerStandardSkillImpl extends AppManagerStandardSkill {

  @Autowired
  private val shardingManager: ShardingManager = null

  @Autowired
  @Qualifier("dtsfMainScheduler")
  private val mainScheduler: Scheduler = null

  private val logger: Logger = LoggerFactory.getLogger("dtsf")

  override def suspend(clusterInfo: ClusterInfo): Unit = {
    try
        if (mainScheduler != null && !mainScheduler.isShutdown) {
          mainScheduler.standby()
        }
    catch {
      case e: Exception =>
        logger.error("run stop main scheduler failed:", e)
    }
    println(s"suspend $clusterInfo")
  }

  override def resume(clusterInfo: ClusterInfo): Unit = {
    try
        if (mainScheduler != null && mainScheduler.isInStandbyMode){
          mainScheduler.start()
        }
    catch {
      case e: Exception =>
        logger.error("run start main scheduler failed:", e)
    }
    println(s"resume $clusterInfo")
  }

  override def close(clusterInfo: ClusterInfo): Unit = {
    println(s"close $clusterInfo")
  }

  override def reconstruction(clusterInfo: ClusterInfo): Unit = {
    println(s"reconstruction $clusterInfo")
    shardingManager.initRedisCache(clusterInfo)
    shardingManager.flustResource()
  }

  override def reallocation(clusterInfo: ClusterInfo): Unit = {
    logger.info(s"reallocation $clusterInfo")
    shardingManager.flushCache()
    shardingManager.saveZksessionInfo(clusterInfo)

    shardingManager.reshardTargets(clusterInfo.clusterVotids)
  }
}
