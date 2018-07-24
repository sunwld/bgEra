package com.collie.bgEra.hpdc.workUnit

import com.collie.bgEra.cloudApp.dtsf.WorkUnitRunable
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo

class LinuxHugePagesUsageCaptcher extends WorkUnitRunable {
  private val TOPIC = "hpdc-hugepage"
  private val SHELL = "LINUX_HUGEPG"

  override def runWork(workUnitInfo: WorkUnitInfo): Unit = {

    /**
      * AnonHugePages:   2500608 kB
      * HugePages_Total:       0
      * HugePages_Free:        0
      * HugePages_Rsvd:        0
      * HugePages_Surp:        0
      * Hugepagesize:       2048 kB
      */
  }
}
