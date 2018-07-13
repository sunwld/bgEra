package com.collie.bgEra.cloudApp.dtsf

import com.collie.bgEra.cloudApp.dtsf.bean.{TaskInfo, TaskResult, WorkUnitInfo, WorkUnitResult}
import com.collie.bgEra.cloudApp.redisCache.bean.ZSetItemBean

import scala.collection.mutable

trait TaskManager {
  def getPreparedTaskList(zkSessionId: String): java.util.List[ZSetItemBean]

  def runTask(taskInfo: TaskInfo): Unit

  def invokWorkUnit(workUnit: WorkUnitInfo): WorkUnitResult

  def finishTask(taskResult: TaskResult, taskScheBeanNm: String): Unit

}
