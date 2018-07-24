package com.collie.bgEra.hpdc.workUnit.bean

import scala.beans.BeanProperty

class HostNetStats{
  @BeanProperty var targetId: String = _
  @BeanProperty var snapId: String = _
  @BeanProperty var netType: String = _
  @BeanProperty var name: String = _
  @BeanProperty var ipk: Long = _
  @BeanProperty var ipkErr: Long = _
  @BeanProperty var opk: Long = _
  @BeanProperty var opkErr: Long = _


  override def toString = s"HostNetStats(targetId=$targetId, snapId=$snapId, netType=$netType, name=$name, ipk=$ipk, ipkErr=$ipkErr, opk=$opk, opkErr=$opkErr)"
}
