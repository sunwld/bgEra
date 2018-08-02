package com.collie.bgEra.hpdc.service.bean

import scala.beans.BeanProperty
import scala.collection.mutable

case class ShellInfo(@BeanProperty var shellName: String,
                     @BeanProperty var targetId: String,
                     @BeanProperty var parameters: mutable.Map[String, String]) {
}
