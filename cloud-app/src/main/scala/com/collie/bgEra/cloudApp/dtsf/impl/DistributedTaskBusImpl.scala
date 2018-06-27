package com.collie.bgEra.cloudApp.dtsf.impl

import com.collie.bgEra.cloudApp.dtsf.DistributedTaskBus
import javax.sql.DataSource

class DistributedTaskBusImpl extends DistributedTaskBus {

  private var currentInstanceId: Long = _

  private var dtfsDataSource: DataSource = _

  override def runBus(): Unit = {

  }

  override def suspend(): Unit = ???

  override def resume(): Unit = ???

  override def close(): Unit = ???

  override def reconstruction(): Unit = ???

  override def reallocation(): Unit = ???
}
