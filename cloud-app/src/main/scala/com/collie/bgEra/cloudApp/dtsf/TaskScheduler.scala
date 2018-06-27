package com.collie.bgEra.cloudApp.dtsf

import java.util.Date

trait TaskScheduler {

  def getNextRunTime(): Date
}
