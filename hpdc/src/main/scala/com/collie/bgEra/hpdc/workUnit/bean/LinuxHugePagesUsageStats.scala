package com.collie.bgEra.hpdc.workUnit.bean

import scala.beans.BeanProperty

class LinuxHugePagesUsageStats {
  @BeanProperty var targetId: String = _
  @BeanProperty var snapId: String = _
  //HugePages_Total , HugePages_Free, HugePages_Rsvd, HugePages_Surp, Hugepagesize
  @BeanProperty var statsResult: (Long, Long, Long, Long, Int) = _

  override def toString = s"LinuxHugePagesUsageStats(targetId=$targetId, snapId=$snapId, statsResult=${statsResult.toString()})"
}


