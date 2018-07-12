package com.collie.bgEra.cloudApp.dtsf

import com.collie.bgEra.cloudApp.appm.AppManagerStandardSkill

trait DistributedTaskBus{

  def runBus(): Unit

  def startScheduler(): Unit

  def stopScheduler(): Unit

}
