package com.collie.bgEra.hpdc.workUnit.bean

import scala.beans.BeanProperty
import scala.collection.mutable

class HostNetStats {
  @BeanProperty var targetId: String = _
  @BeanProperty var snapId: String = _
  //name -> (snapid,mtu,ipks,ierrs,opks,oerrs)
  @BeanProperty var statsResult: java.util.Map[String, (String, Int, Long, Long, Long, Long)] = _


  override def toString = s"HostNetStats(targetId=$targetId, snapId=$snapId, statsResult=${statsResult.toString()})"
}

object HostNetStats {
  val name = "name"
  val mtu = "mtu"
  val ipks = "ipks"
  val ierrs = "ierrs"
  val opks = "opks"
  val oerrs = "oerrs"
}

