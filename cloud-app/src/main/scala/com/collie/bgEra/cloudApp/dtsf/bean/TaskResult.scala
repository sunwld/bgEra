package com.collie.bgEra.cloudApp.dtsf.bean

class TaskResult {
  val SUCCESS = "success"
  val EXCEPTION = "exception"
  val SUCCESS_WITH_ERRORS = "success_with_errors"

  var result: String = _
  var workUnitResults: Array[WorkUnitResult] = _
}
