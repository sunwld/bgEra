package com.collie.bgEra.hpdc.workUnit.bean

import scala.beans.BeanProperty

class SwapIOStats {
  @BeanProperty var targetId: String = _
  @BeanProperty var snapId: String = _
  // swapins,swapouts
  @BeanProperty var statsResult: (Double, Double) = _


  override def toString = s"SwapIOStats(targetId=$targetId, snapId=$snapId, statsResult=${statsResult.toString()})"
}

object SwapIOStats {
  val swapIn = "swapIn"
  val swapOut = "swapOut"
}