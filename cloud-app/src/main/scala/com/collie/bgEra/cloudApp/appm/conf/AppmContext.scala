package com.collie.bgEra.cloudApp.appm.conf


case class AppmContext(var projectName: String, var zkUrls: String,
                  var minLiveServCount: Int, var clusterInitServCount: Int) {
}
