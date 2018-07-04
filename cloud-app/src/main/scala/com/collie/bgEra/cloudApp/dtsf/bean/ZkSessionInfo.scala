package com.collie.bgEra.cloudApp.dtsf.bean

import scala.beans.BeanProperty

case class ZkSessionInfo(@BeanProperty  zksessionId: String,@BeanProperty var isMaster: Boolean) {

}
