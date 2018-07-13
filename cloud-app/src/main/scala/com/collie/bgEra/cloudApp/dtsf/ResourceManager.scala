package com.collie.bgEra.cloudApp.dtsf

import java.util.Properties

import com.collie.bgEra.cloudApp.dtsf.bean.{DataSourceResource, HostSshConnPoolResource, JmxConnPoolResource}
import org.apache.ibatis.session.SqlSessionFactory

trait ResourceManager {

  def getDataSourceResource(resourceName: String): SqlSessionFactory

  def initDataSourceResource(name: String, defaultProp: Properties): SqlSessionFactory

  def getHostSshConnPoolResource(resourceName: String): HostSshConnPoolResource

  def initHostSshConnPoolResource(resourceContext: Properties): Unit

  def getJmxConnPoolResource(resourceName: String): JmxConnPoolResource

  def initJmxConnPoolResource(resourceContext: Properties): Unit
}
