package com.collie.bgEra.hpdc.service.bean

import scala.beans.BeanProperty
import scala.collection.mutable

/**
  * @param lastSnapId last_snapid
  * @param statsVal   (statid,(snapid,snapval))
  */
case class CalculateIncacheStatsValue[T](@BeanProperty var lastSnapId: String,
                                         @BeanProperty var statsVal: mutable.Map[T, (String, Double)]) {
}
