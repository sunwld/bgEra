package com.collie.bgEra.cloudApp.dtsf.bean

import java.util.Date

import scala.beans.BeanProperty

class TaskErrorBean private (@BeanProperty val logDate: Date, @BeanProperty val taskName: String,
                         @BeanProperty val targetId: String, @BeanProperty val wkunitName: String,
                         @BeanProperty val zkSession: String, @BeanProperty val errmsg: String) {

}

object TaskErrorBean{
  def apply(logDate: Date, taskName: String,
          targetId: String, wkunitName: String,
          zkSession: String, errmsg: String) = {

    var msg = errmsg
    if(msg != null && msg.length() > 500){
      msg = msg.substring(0,500)
    }

    new TaskErrorBean(logDate,taskName,targetId,wkunitName,zkSession,msg)
  }
}
