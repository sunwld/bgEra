package com.collie.bgEra.cloudApp.dtsf.schedules

import java.util.Date

import com.collie.bgEra.cloudApp.dtsf.TaskSchedule
import com.collie.bgEra.commons.util.DateUtils
import org.springframework.stereotype.Component

@Component("interval_1s")
class Interval1sSchedule extends TaskSchedule{
  override def getNextRunTime(lastFinishTime: Date): Date = {
    DateUtils.dateAdd(DateUtils.INTERVAL_SECOND,lastFinishTime,1)
  }
}
