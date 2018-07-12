package com.collie.bgEra.cloudApp.dtsf.impl

import com.collie.bgEra.cloudApp.dtsf.TaskManager
import com.collie.bgEra.cloudApp.dtsf.bean.TaskInfo
import org.quartz.{Job, JobExecutionContext}

class QuartzJob extends Job{
  override def execute(context: JobExecutionContext): Unit = {
    val jobDataMap = context.getJobDetail().getJobDataMap()
    val taskManager = jobDataMap.get("taskManager").asInstanceOf[TaskManager]
    val task = jobDataMap.get("task").asInstanceOf[TaskInfo]
    taskManager.runTask(task)
  }
}
