package com.collie.bgEra.opdc.workUnitRunable

import com.collie.bgEra.cloudApp.dtsf.WorkUnitRunable
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo
import org.springframework.stereotype.Component

@Component("cpuStatsCaptcher")
class CpuStatsCaptcher extends WorkUnitRunable{
  override def runWork(workUnitInfo: WorkUnitInfo): Unit = {
  }
}
