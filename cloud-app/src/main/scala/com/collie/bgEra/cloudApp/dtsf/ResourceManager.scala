package com.collie.bgEra.cloudApp.dtsf

import java.util.Properties

import com.alibaba.druid.pool.DruidDataSource
import com.collie.bgEra.cloudApp.dtsf.bean.JmxConnPoolResource
import com.collie.bgEra.cloudApp.ssh2Pool.{Ssh2Session, Ssh2SessionPool}
import org.apache.ibatis.session.SqlSessionFactory

trait ResourceManager {

  def getDataSourceResource(targetId: String): SqlSessionFactory

  def initDataSourceResource(name: String): (SqlSessionFactory,DruidDataSource)

  def putSqlSessionFactoy(name: String,ds:DruidDataSource,factory: SqlSessionFactory): Unit

  def flushAllDataSourceResource(): Unit

  def getHostSshConnPoolResource(targetId: String): Ssh2Session

  def initHostSshConnPoolResource(targetId: String): Ssh2SessionPool

  def flushAllHostSshConnPoolResource(): Unit

  def getJmxConnPoolResource(targetId: String): JmxConnPoolResource

  def initJmxConnPoolResource(resourceContext: Properties): Unit

  def flushAllJmxConnPoolResource(): Unit
}
