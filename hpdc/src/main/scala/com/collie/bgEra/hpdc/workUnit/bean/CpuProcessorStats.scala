package com.collie.bgEra.hpdc.workUnit.bean

import java.util

import scala.beans.BeanProperty
import scala.collection.mutable

class CpuProcessorStats {
  @BeanProperty var targetId: String = _
  @BeanProperty var snapId: String = _
  @BeanProperty var statsResult:Map[Int,(Int, Float, Float, Float, Float, Int)] = _

  override def toString = s"CpuProcessorStats(targetId=$targetId, snapId=$snapId, statsResult=$statsResult)"
}
