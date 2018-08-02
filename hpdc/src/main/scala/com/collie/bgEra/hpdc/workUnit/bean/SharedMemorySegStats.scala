package com.collie.bgEra.hpdc.workUnit.bean

import java.util

import scala.beans.BeanProperty
import scala.collection.JavaConversions._

class SharedMemorySegStats {
  @BeanProperty var targetId: String = _
  @BeanProperty var snapId: String = _
  //(shmid|ID,key|KEY,owner|OWNER,bytes|SEGSZ,cpid|CPID,lpid|LPID,attached|ATIME,detached|DTIME,changed|CTIME,status|MODE)
  @BeanProperty var statsResult: java.util.List[(Long, String, String, Long, Long, Long, String, String, String,String)] = _

  override def toString() = {
    s"SharedMemorySegStats(targetId=$targetId, snapId=$snapId, statsResult=${statsResult.toString()})"}

}
