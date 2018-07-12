package com.collie.bgEra.cloudApp.dtsf.schedules

import java.util.Date

import com.collie.bgEra.cloudApp.dtsf.TaskSchedule
import com.collie.bgEra.commons.util.{DateUtils, SerialNumberUtils}
import org.springframework.stereotype.Component

@Component("trunc_1min")
class NextTrunc1MinSchedule extends TaskSchedule{
  override def getNextRunTime(lastFinishTime: Date): Date = {
    SerialNumberUtils.getDateTimeByTrunc1min(DateUtils.dateAdd(DateUtils.INTERVAL_MINUTE,lastFinishTime,1))
  }
}
