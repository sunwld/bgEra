package com.collie.bgEra.cloudApp.dtsf

import java.util.Properties

import com.collie.bgEra.cloudApp.dtsf.bean.{DataSourceResource, HostSshConnPoolResource, JmxConnPoolResource}

trait ResourceManager {

  def getDataSourceResource(resourceName: String): DataSourceResource

  def initDataSourceResource(resourceContext: Properties): Unit

  def getHostSshConnPoolResource(resourceName: String): HostSshConnPoolResource

  def initHostSshConnPoolResource(resourceContext: Properties): Unit

  def getJmxConnPoolResource(resourceName: String): JmxConnPoolResource

  def initJmxConnPoolResource(resourceContext: Properties): Unit
}
