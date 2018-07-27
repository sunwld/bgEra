package com.collie.bgEra.hpdc.workUnit.bean

import scala.beans.BeanProperty
import scala.collection.mutable

class FilesystemUsageStats {

  @BeanProperty var targetId: String = _
  @BeanProperty var snapId: String = _

  /**
    * Map:Key FsName,the block dev name
    * (String,String,Long,Long,Float)
    * fsName: the os block dev name
    * fsMountPoint: mounted dir
    * usedKb:
    * freeKb:
    * usedPerc: %
    */
  @BeanProperty var statsResult: Map[String, (String, String, Double, Double, Float)] = _

  override def toString = s"FilesystemUsageStats(targetId=$targetId, snapId=$snapId, statsResult=$statsResult)"
}


