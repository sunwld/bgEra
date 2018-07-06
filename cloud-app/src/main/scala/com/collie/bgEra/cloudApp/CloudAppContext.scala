package com.collie.bgEra.cloudApp

import com.collie.bgEra.cloudApp.appm.ClusterInfo

import scala.beans.BeanProperty

case class CloudAppContext(val projectName: String,
                      val minLiveServCount: Int, val clusterInitServCount: Int) {

  @BeanProperty
  var clusterInfo: ClusterInfo = null

}
