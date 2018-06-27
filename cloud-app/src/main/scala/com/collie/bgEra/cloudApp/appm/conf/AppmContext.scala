package com.collie.bgEra.cloudApp.appm.conf

import com.collie.bgEra.cloudApp.appm.AppManagerStandardSkill

case class AppmContext(var projectName: String, var zkUrls: String,
                  var minLiveServCount: Int, var clusterInitServCount: Int) {
}
