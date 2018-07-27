package com.collie.bgEra.hpdc.workUnit.bean

import java.util

import scala.beans.BeanProperty
import scala.collection.mutable.ListBuffer

class ProcessListStats {
  @BeanProperty var targetId: String = _
  @BeanProperty var snapId: String = _
  //(PID,S,PRI,NI,WCHAN,TTY,TIME,cmd,C,RSS[LINUX]|SZ[SOLARIS|AIX])
  @BeanProperty var statsResult:ListBuffer[(Int,String,String,String,String,String,String,String,Float,Long)] = _
}
