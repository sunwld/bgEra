package com.collie.bgEra.cloudApp.dtsf.bean

import scala.beans.BeanProperty

/**
  * WorkUnitResult类，任务链上单个任务单元的运行结果信息
  */
class WorkUnitResult {
  @BeanProperty var result: String = _
  @BeanProperty var message: String = _
  @BeanProperty var exception: Exception = _
}

object WorkUnitResult{
  val SUCCESS = "success"
  val EXCEPTION = "exception"
}