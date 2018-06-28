package com.collie.bgEra.cloudApp.dtsf.bean

import scala.beans.BeanProperty

class ResourceInfo {
  @BeanProperty var resourceName: String = _
  @BeanProperty var resourceType: String = _
}

object ResourceInfo {
  val DBDATASOURCE = "db_datasource"
  val DBDATASOURCE_SHORT = "db_datasource_short"
  val HOSTSSHTPOOL = "host_ssh_pool"
  val HOSTSSHTPOOL_SHORT = "host_ssh_pool_short"
  val JMXCONNPOOL = "jmx_conn_pool"
}


