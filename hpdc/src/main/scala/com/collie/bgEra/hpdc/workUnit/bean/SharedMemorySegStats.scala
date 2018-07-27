package com.collie.bgEra.hpdc.workUnit.bean

import java.util

import scala.beans.BeanProperty
import scala.collection.mutable.ListBuffer

class SharedMemorySegStats {
  @BeanProperty var targetId: String = _
  @BeanProperty var snapId: String = _
  //(shmid|ID,key|KEY,owner|OWNER,bytes|SEGSZ,cpid|CPID,lpid|LPID,attached|ATIME,detached|DTIME,changed|CTIME,status|MODE)
  @BeanProperty var statsResult: ListBuffer[(Long, String, String, Long, Long, Long, String, String, String,String)] = _
}
