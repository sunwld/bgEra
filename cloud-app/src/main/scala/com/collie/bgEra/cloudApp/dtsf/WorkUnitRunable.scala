package com.collie.bgEra.cloudApp.dtsf

import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo

trait WorkUnitRunable {

  def runWork(workUnitInfo: WorkUnitInfo): Unit
}
