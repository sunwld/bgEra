package com.collie.bgEra.hpdc.workUnit.bean

import scala.beans.BeanProperty

class LinuxHugePagesUsageStats {
  @BeanProperty var targetId: String = _
  @BeanProperty var snapId: String = _
  //targetId,snapId,HugePages_Total , HugePages_Free, HugePages_Rsvd, HugePages_Surp, Hugepagesize
  @BeanProperty var statsResult: (String, String, Long, Long, Long, Long, Int) = _
}


