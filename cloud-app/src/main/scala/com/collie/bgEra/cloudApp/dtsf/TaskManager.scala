package com.collie.bgEra.cloudApp.dtsf

import com.collie.bgEra.cloudApp.dtsf.bean.{TaskResult, WorkUnitResult}
import org.springframework.util.StopWatch.TaskInfo

import scala.collection.mutable

trait TaskManager {
  def getPreparedTaskList(zkSessionId: String): mutable.Seq[mutable.Seq[String]]

  def runTask(taskInfo: TaskInfo): TaskResult

  def invokWorkUnit(workUnitName: String): WorkUnitResult

  def finishTask(taskResult: TaskResult, taskScheBeanNm: String): Unit

}
