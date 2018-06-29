package com.collie.bgEra.cloudApp.dtsf.impl

import com.collie.bgEra.cloudApp.appm.{AppManagerStandardSkill, ClusterInfo}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.stereotype.Component

@Component("appManagerStandardSkill")
class AppManagerStandardSkillImpl extends AppManagerStandardSkill {
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
    println(s"reallocation $clusterInfo")
  }
}
