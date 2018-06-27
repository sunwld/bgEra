package com.collie.bgEra.cloudApp.dtsf.bean

class WorkUnitResult {
  val SUCCESS = "success"
  val EXCEPTION = "exception"

  var result: String = _
  var message: String = _
  var exception: Exception = _
}
