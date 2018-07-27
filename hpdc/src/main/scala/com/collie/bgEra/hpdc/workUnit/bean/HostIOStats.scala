package com.collie.bgEra.hpdc.workUnit.bean

import java.util

import scala.beans.BeanProperty

class HostIOStats {

  @BeanProperty var targetId: String = _
  @BeanProperty var snapId: String = _
  /**
    * K: Device name
    * V:(DeviceName,IOKB,avgWait,busy%,snapCount)
    *
    */
  @BeanProperty var statsResult: Map[String, (String, Double, Double, Float, Int)] = _


  override def toString = s"HostIOStats(targetId=$targetId, snapId=$snapId, statsResult=$statsResult)"
}
