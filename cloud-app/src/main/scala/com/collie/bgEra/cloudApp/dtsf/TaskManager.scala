package com.collie.bgEra.cloudApp.dtsf

import com.collie.bgEra.cloudApp.dtsf.bean.{TaskInfo, TaskResult, WorkUnitInfo, WorkUnitResult}

import scala.collection.mutable

trait TaskManager {
  def getPreparedTaskList(zkSessionId: String): java.util.List[TaskInfo]

  def runTask(taskInfo: TaskInfo): Unit

  def invokWorkUnit(workUnit: WorkUnitInfo): WorkUnitResult

  def finishTask(taskResult: TaskResult, taskScheBeanNm: String): Unit

}
