package com.collie.bgEra.cloudApp.bpq

import scala.beans.BeanProperty

case class SqlResult() {
  @BeanProperty var success: Boolean = false
  @BeanProperty var modifyCount: Int = 0
  @BeanProperty var finish: Boolean = false


  override def toString = s"SqlResult($success, $modifyCount, $finish)"
}
