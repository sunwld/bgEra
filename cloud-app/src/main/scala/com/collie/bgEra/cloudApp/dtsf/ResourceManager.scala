package com.collie.bgEra.cloudApp.dtsf

import java.util.Properties

import com.collie.bgEra.cloudApp.dtsf.bean.{JmxConnPoolResource}
import com.collie.bgEra.cloudApp.ssh2Pool.SshSession
import org.apache.commons.pool2.impl.GenericObjectPool
import org.apache.ibatis.session.SqlSessionFactory

trait ResourceManager {

  def getDataSourceResource(targetId: String): SqlSessionFactory

  def initDataSourceResource(name: String): SqlSessionFactory

  def getHostSshConnPoolResource(targetId: String): GenericObjectPool[SshSession]

  def initHostSshConnPoolResource(targetId: String): GenericObjectPool[SshSession]

  def getJmxConnPoolResource(targetId: String): JmxConnPoolResource

  def initJmxConnPoolResource(resourceContext: Properties): Unit
}
