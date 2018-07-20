package com.collie.bgEra.cloudApp.dtsf.bean

import scala.beans.BeanProperty
import scala.collection.mutable

class TargetInfo {
  @BeanProperty var name: String = _
  @BeanProperty var description: String = _
  @BeanProperty var shardingValue: Int = _
  @BeanProperty var resourceMap: mutable.Map[String, ResourceInfo] = _


  override def toString = s"TargetInfo(name=$name, description=$description, shardingValue=$shardingValue, resourceMap=$resourceMap)"
}
