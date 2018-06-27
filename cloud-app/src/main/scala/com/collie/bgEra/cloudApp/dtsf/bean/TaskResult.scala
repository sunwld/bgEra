package com.collie.bgEra.cloudApp.dtsf.bean

/**
  * TaskResult类： TASK任务链 的执行结果
  */
class TaskResult {
  var result: String = _
  var workUnitResults: Array[WorkUnitResult] = _
}

object TaskResult{
  val SUCCESS_WITH_ERRORS = "success_with_errors"
  val SUCCESS = "success"
  val EXCEPTION = "exception"
}