package com.collie.bgEra.cloudApp.dtsf.bean

import java.util.Date

import scala.beans.BeanProperty

class TaskErrorBean private (@BeanProperty var logDate: Date, @BeanProperty var taskName: String,
                    @BeanProperty var targetId: String, @BeanProperty var wkunitName: String,
                    @BeanProperty var zkSession: String, @BeanProperty var errmsg: String) {

}

object TaskErrorBean {
  def apply(logDate: Date, taskName: String,
            targetId: String, wkunitName: String,
            zkSession: String, errmsg: String) = {

    var msg = errmsg
    if (msg != null && msg.length() > 500) {
      msg = msg.substring(0, 500)
    }

    new TaskErrorBean(logDate, taskName, targetId, wkunitName, zkSession, msg)
  }
}
