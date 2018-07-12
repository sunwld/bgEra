package com.collie.bgEra.cloudApp.dtsf.schedules

import java.util.Date

import com.collie.bgEra.cloudApp.dtsf.TaskSchedule
import com.collie.bgEra.commons.util.{DateUtils, SerialNumberUtils}
import org.springframework.stereotype.Component

@Component("trunc_10s")
class NextTrunc10sSchedule extends TaskSchedule{
  override def getNextRunTime(lastFinishTime: Date): Date = {
    SerialNumberUtils.getDateTimeByTrunc10s(DateUtils.dateAdd(DateUtils.INTERVAL_SECOND,lastFinishTime,10))
  }
}
