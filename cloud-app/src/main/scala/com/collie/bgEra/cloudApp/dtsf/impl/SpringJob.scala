package com.collie.bgEra.cloudApp.dtsf.impl

import com.collie.bgEra.cloudApp.appm.ZApplicationManager
import com.collie.bgEra.cloudApp.context.CloudAppContext
import com.collie.bgEra.cloudApp.dtsf.mapper.TaskMapper
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Component
import org.springframework.scheduling.annotation.Scheduled


@Component
class SpringJob {

  @Autowired
  private val taskMapper: TaskMapper = null

  @Autowired
  @Qualifier("zApplicationManager")
  val zkAppManager: ZApplicationManager = null

  @Scheduled(fixedDelay = 2000)
  def flushTaskStatusTask(): Unit = {
    val clusterInfo = zkAppManager.clusterInfo
    if(clusterInfo != null){
      taskMapper.flushTaskStatusToDB(clusterInfo.currentVotid)
    }
  }

}
