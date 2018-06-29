package com.collie.bgEra.cloudApp.dtsf.bean

import scala.beans.BeanProperty

class TargetInfo {
  @BeanProperty var name: String = _
  @BeanProperty var description: String = _
  @BeanProperty var shardingValue: Int = _

  override def toString = s"TargetInfo($name, $description, $shardingValue)"
}
