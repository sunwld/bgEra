package com.collie.bgEra.cloudApp.dtsf.schedulers

import java.util.Date

import com.collie.bgEra.cloudApp.dtsf.TaskScheduler
import org.springframework.stereotype.Component

@Component("interval_1s")
class Interval1sScheduler extends TaskScheduler{
  override def getNextRunTime(): Date = {
    null
  }
}
