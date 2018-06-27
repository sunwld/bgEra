package com.collie.bgEra.cloudApp.appm

import java.util

case class ClusterInfo(var currentVotid:String,var clusterVotids: util.List[String],
                       var minClusterMems:Int,var isMaster:Boolean,
                      var appName: String) {
}
