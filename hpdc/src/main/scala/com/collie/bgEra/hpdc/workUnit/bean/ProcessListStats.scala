package com.collie.bgEra.hpdc.workUnit.bean

import java.util

import scala.beans.BeanProperty
import scala.collection.mutable.ListBuffer

class ProcessListStats {
  @BeanProperty var targetId: String = _
  @BeanProperty var snapId: String = _
  //(PID,PPID,S,PRI,NI,WCHAN,TTY,TIME,cmd,C,RSS,SZ)
  @BeanProperty var statsResult: java.util.List[(Long, Long, String, String, String, String, String, String, String, Float, Long, Long)] = _

  override def toString = s"ProcessListStats(targetId=$targetId, snapId=$snapId, statsResult=${statsResult.toString()})"
}

object ProcessListStats {

  val pid = "PID"
  val ppid = "PPID"
  val stat = "S"
  val pri = "PRI"
  val ni = "NI"
  val wchan = "WCHAN"
  val tty = "TTY"
  val time = "TIME"
  val cmd = "CMD"
  val cpu = "CPU"
  val sz = "SIZE"
  val rss= "RSS"
}