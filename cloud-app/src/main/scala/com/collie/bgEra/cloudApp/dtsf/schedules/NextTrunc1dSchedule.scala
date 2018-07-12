package com.collie.bgEra.cloudApp.dtsf.schedules

import java.util.Date

import com.collie.bgEra.cloudApp.dtsf.TaskSchedule
import com.collie.bgEra.commons.util.{DateUtils, SerialNumberUtils}
import org.springframework.stereotype.Component

@Component("trunc_1d")
class NextTrunc1dSchedule extends TaskSchedule{
  override def getNextRunTime(lastFinishTime: Date): Date = {
    SerialNumberUtils.getDateTimeBySerialTrunc1d(DateUtils.dateAdd(DateUtils.INTERVAL_DAY,lastFinishTime,1))
  }
}
