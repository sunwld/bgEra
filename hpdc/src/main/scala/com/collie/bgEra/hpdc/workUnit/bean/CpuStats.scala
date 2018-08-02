package com.collie.bgEra.hpdc.workUnit.bean

import scala.beans.BeanProperty

class CpuStats {
  @BeanProperty var targetId: String = _
  @BeanProperty var snapId: String = _
  //  @BeanProperty var user: Float = _
  //  @BeanProperty var sys: Float = _
  //  @BeanProperty var cpuWait: Float = _
  //  @BeanProperty var idle: Float = _
  //user%,sys%,cpuWait%,idle%
  @BeanProperty var statsResult: (Float, Float, Float, Float) = _

  override def toString = s"CpuStats(targetId=$targetId, snapId=$snapId, statsResult=${statsResult.toString()})"
}
