package com.collie.bgEra.hpdc.workUnit.bean

import com.collie.bgEra.commons.TextBufferContentExtractor

import scala.beans.BeanProperty
import scala.collection.mutable

class CpuProcessorStats {

  @BeanProperty var targetId: String = _
  @BeanProperty var snapId: String = _
  //cpuid -> sysp,userp,iowaitp,idlep,snapcount
  @BeanProperty var statsResult: java.util.Map[Int, (Float, Float, Float, Float, Int)] = _
  override def toString = s"CpuProcessorStats(targetId=$targetId, snapId=$snapId, statsResult=${statsResult.toString()})"
}

object CpuProcessorStats {

  val cpuId = ("cpuId", TextBufferContentExtractor.intType)
  val sys = ("sys", TextBufferContentExtractor.floatType)
  val ioWait = ("ioWait", TextBufferContentExtractor.floatType)
  val idle = ("idle", TextBufferContentExtractor.floatType)
  val user = ("user", TextBufferContentExtractor.floatType)
}
