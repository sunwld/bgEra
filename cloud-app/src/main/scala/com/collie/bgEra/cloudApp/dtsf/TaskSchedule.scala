package com.collie.bgEra.cloudApp.dtsf

import java.util.Date

trait TaskSchedule {

  def getNextRunTime(lastFinishTime: Date): Date
}
