package com.collie.bgEra.cloudApp.dtsf.bean

import scala.beans.BeanProperty

/**
  * WorkUnitResult类，任务链上单个任务单元的运行结果信息
  */
class WorkUnitResult private(@BeanProperty var result: String,@BeanProperty var message: String,@BeanProperty var exception: Exception) {
}

object WorkUnitResult{
  val SUCCESS = "success"
  val EXCEPTION = "exception"

  def apply(result: String, message: String, ex: Exception) = {
    var msg = message
    if(msg != null && msg.length() > 500){
      msg = msg.substring(0,500)
    }
    new WorkUnitResult(result,msg,ex)
  }
}