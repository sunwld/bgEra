package com.collie.bgEra.cloudApp.appm

import java.util

import scala.collection.mutable

case class ClusterInfo(var currentVotid:String,var clusterVotids: mutable.Seq[String],
                       var minClusterMems:Int,var isMaster:Boolean,
                      var appName: String) {
}
