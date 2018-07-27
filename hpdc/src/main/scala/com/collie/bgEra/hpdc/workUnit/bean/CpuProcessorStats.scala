package com.collie.bgEra.hpdc.workUnit.bean

import com.fasterxml.jackson.databind.BeanProperty

class CpuProcessorStats {

  @BeanProperty var cpuId: Int = _
  @BeanProperty var user: Float = _
  @BeanProperty var sys: Float = _
  @BeanProperty var iowait: Float = _
  @BeanProperty var idle: Float = _
  override def toString = s"CpuProcessorStats($cpuId, $user, $sys, $iowait, $idle)"
}
