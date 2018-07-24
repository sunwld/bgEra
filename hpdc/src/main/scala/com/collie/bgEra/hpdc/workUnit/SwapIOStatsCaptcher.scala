package com.collie.bgEra.hpdc.workUnit

import com.collie.bgEra.cloudApp.dtsf.WorkUnitRunable
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo

class SwapIOStatsCaptcher extends WorkUnitRunable {
  private val TOPIC = "hpdc-swapio"
  private val SHELL = "SWAP_PAGESTAT"
  override def runWork(workUnitInfo: WorkUnitInfo): Unit = {
    /**
      * OSTYPE:LINUX
      * 0 pages swapped in
      * 0 pages swapped out
      */

    /**
      * OSTYPE:AIX
      * 4924134 paging space page ins
      * 7024230 paging space page outs
      */

    /**
      * OSTYPE:SOLARIS
      * 0 pages swapped in
      * 0 pages swapped out
      */



  }
}
