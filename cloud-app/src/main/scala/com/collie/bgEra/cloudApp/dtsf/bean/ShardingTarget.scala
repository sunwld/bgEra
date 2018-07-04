package com.collie.bgEra.cloudApp.dtsf.bean

import scala.beans.BeanProperty

case class ShardingTarget(@BeanProperty var zkSessionId: String,@BeanProperty var dtfTargetId: String ) {

}
