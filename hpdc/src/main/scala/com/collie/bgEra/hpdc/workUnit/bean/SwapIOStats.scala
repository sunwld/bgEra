package com.collie.bgEra.hpdc.workUnit.bean

import scala.beans.BeanProperty

class SwapIOStats {
  @BeanProperty var targetId: String = _
  @BeanProperty var snapId: String = _
  //targetId,snapId,swapins,swapouts
  @BeanProperty var statsResult: (String, String, Double, Double) = _
}

object SwapIOStats {
  val swapIn = "swapIn"
  val swapOut = "swapOut"
}