package com.collie.bgEra.cloudApp.dtsf

import com.collie.bgEra.cloudApp.appm.AppManagerStandardSkill

trait DistributedTaskBus extends AppManagerStandardSkill{

  def runBus(): Unit

}
