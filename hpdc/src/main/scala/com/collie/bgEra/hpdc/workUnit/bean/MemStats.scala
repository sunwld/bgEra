package com.collie.bgEra.hpdc.workUnit.bean

import scala.beans.BeanProperty

class MemStats{
  @BeanProperty var targetId: String = _
  @BeanProperty var snapId: String = _
  @BeanProperty var memTotal: Long = _
  @BeanProperty var memFree: Long = _
  @BeanProperty var cacheInuse: Long = _
  @BeanProperty var swapTotal: Long = _
  @BeanProperty var swapFree: Long = _


  override def toString = s"MemStats(targetId=$targetId, snapId=$snapId, memTotal=$memTotal, memFree=$memFree, cacheInuse=$cacheInuse, swapTotal=$swapTotal, swapFree=$swapFree)"
}
