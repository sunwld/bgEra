package com.collie.bgEra.cloudApp.dtsf.impl

import com.collie.bgEra.cloudApp.CloudAppContext
import com.collie.bgEra.cloudApp.dtsf.mapper.TaskMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.scheduling.annotation.Scheduled


@Component
class SpringJob {

  @Autowired
  private val taskMapper: TaskMapper = null

  @Autowired
  val context: CloudAppContext = null

  @Scheduled(fixedDelay = 2000)
  def flushTaskStatusTask(): Unit = {
    val clusterInfo = context.appmClusterInfo
    if(clusterInfo != null){
      taskMapper.flushTaskStatusToDB(clusterInfo.currentVotid)
    }
  }

}
