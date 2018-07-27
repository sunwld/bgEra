package com.collie.bgEra.hpdc.workUnit.bean

import java.util

import scala.beans.BeanProperty
import scala.collection.mutable

class NetworkErrorsStats {
  @BeanProperty var targetId: String = _
  @BeanProperty var snapId: String = _
  //statid->(snapid,diffSeconds,diffVal)
  @BeanProperty var statsResult:mutable.Map[String,(String,Long,Double)] = _

  override def toString = s"NetworkErrorsStats(targetId=$targetId, snapId=$snapId, statsResult=$statsResult)"
}
