package com.collie.bgEra.hpdc.workUnit

import com.collie.bgEra.cloudApp.dtsf.WorkUnitRunable
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo
import org.springframework.stereotype.Component

@Component("networkStatsCaptcher")
class NetworkStatsCaptcher extends WorkUnitRunable{
  override def runWork(workUnitInfo: WorkUnitInfo): Unit = {
  }
}