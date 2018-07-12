package com.collie.bgEra.cloudApp.dsla

import java.util.concurrent.CountDownLatch

import scala.beans.BeanProperty

case class LatchBean(@BeanProperty var latchKey: String,
                     @BeanProperty var countDownLatch: CountDownLatch) {

}
