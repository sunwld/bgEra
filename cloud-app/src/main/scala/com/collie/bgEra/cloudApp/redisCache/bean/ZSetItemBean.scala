package com.collie.bgEra.cloudApp.redisCache.bean

import scala.beans.BeanProperty

case class ZSetItemBean(@BeanProperty id: String, @BeanProperty var score: Double) {

}
