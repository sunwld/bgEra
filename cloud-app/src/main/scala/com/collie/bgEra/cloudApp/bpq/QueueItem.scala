package com.collie.bgEra.cloudApp.bpq

import org.apache.ibatis.session.SqlSessionFactory

import scala.beans.BeanProperty

class QueueItem private(@BeanProperty val factoryName: String,
                        @BeanProperty val result: SqlResult,
                        @BeanProperty val sqlItems: SqlItem*) {

  var failedCount = 0

  override def toString = s"QueueItem($failedCount, $factoryName, $result, $sqlItems)"
}

object QueueItem{
  def apply(actoryName: String,sqlItems: SqlItem*): QueueItem ={
    new QueueItem(actoryName,SqlResult(),sqlItems:_*)
  }

  def apply(actoryName: String, result: SqlResult,sqlItems: SqlItem*): QueueItem ={
    new QueueItem(actoryName,result,sqlItems:_*)
  }
}

