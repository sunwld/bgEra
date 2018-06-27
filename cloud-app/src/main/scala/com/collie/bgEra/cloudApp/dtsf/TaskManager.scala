package com.collie.bgEra.cloudApp.dtsf

import com.collie.bgEra.cloudApp.dtsf.bean.{ShardingInfo, TaskResult, WorkUnitResult}
import org.springframework.util.StopWatch.TaskInfo

trait TaskManager {

  def getPreparedTaskList(shardingInfo: ShardingInfo): List[List[String]]

  def getPreparedTaskList(instId: Long): List[List[String]]

  def runTask(taskInfo: TaskInfo): TaskResult

  def invokWorkUnit(workUnitName: String): WorkUnitResult

  def finishTask(taskResult: TaskResult, taskScheBeanNm: String): Unit

}
