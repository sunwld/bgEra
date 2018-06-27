package com.collie.bgEra.cloudApp.dtsf.bean

/**
  * WorkUnitResult类，任务链上单个任务单元的运行结果信息
  */
class WorkUnitResult {
  var result: String = _
  var message: String = _
  var exception: Exception = _
}

object WorkUnitResult{
  val SUCCESS = "success"
  val EXCEPTION = "exception"
}