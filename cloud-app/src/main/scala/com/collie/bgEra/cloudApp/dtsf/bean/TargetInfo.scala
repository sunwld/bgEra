package com.collie.bgEra.cloudApp.dtsf.bean

import scala.beans.BeanProperty

class TargetInfo {
  @BeanProperty var name: String = _
  @BeanProperty var discription: String = _
  @BeanProperty var shardingValue: Int = _
}
