package com.collie.bgEra.hpdc.workUnit.bean

import scala.beans.BeanProperty

class MemStats {
  @BeanProperty var targetId: String = _
  @BeanProperty var snapId: String = _
  // memTotal,memFree,cacheInuse,swapTotal,swapFree
  @BeanProperty var statsResult: (Long, Long, Long, Long, Long) = _
  
  override def toString = s"MemStats(targetId=$targetId, snapId=$snapId, statsResult=${statsResult.toString()})"
}
