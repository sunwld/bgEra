package com.collie.bgEra.hpdc.workUnit.bean

import java.util

import scala.beans.BeanProperty
import scala.collection.mutable

class HostIOStats {

  @BeanProperty var targetId: String = _
  @BeanProperty var snapId: String = _
  /**
    * K: Device name
    * V:(IOKB,avgWait,busy%,snapCount)
    *
    */
  // deviceName -> IOKB,avgWait,busy%,snapCount
  @BeanProperty var statsResult: java.util.Map[String, (Double, Double, Float, Int)] = _


  override def toString = s"HostIOStats(targetId=$targetId, snapId=$snapId, statsResult=${statsResult.toString()})"
}
