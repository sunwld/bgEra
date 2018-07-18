package com.collie.bgEra.cloudApp.bpq

import org.apache.ibatis.session.SqlSessionFactory

import scala.beans.BeanProperty

class SqlItem(@BeanProperty val sqlStatement: String,
                   @BeanProperty val sqlType: Int,
                   @BeanProperty val params: Any) {


  override def toString = s"SqlItem($sqlStatement, $sqlType, $params)"
}

object SqlItem{
  val INSERT_SQL = 1 //"insert"
  val UPDATE_SQL = 2 //"update"
  val DELETE_SQL = 3 //"delete"

  def apply(sqlStatement: String,sqlType: Int,params: Any): SqlItem ={
    new SqlItem(sqlStatement,sqlType,params)
  }
}
