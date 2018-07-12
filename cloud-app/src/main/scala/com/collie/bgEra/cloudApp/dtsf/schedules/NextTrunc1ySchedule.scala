package com.collie.bgEra.cloudApp.dtsf.schedules

import java.util.Date

import com.collie.bgEra.cloudApp.dtsf.TaskSchedule
import com.collie.bgEra.commons.util.{DateUtils, SerialNumberUtils}
import org.springframework.stereotype.Component

@Component("trunc_1y")
class NextTrunc1ySchedule extends TaskSchedule{
  override def getNextRunTime(lastFinishTime: Date): Date = {
    SerialNumberUtils.getDateTimeBySerialTrunc1y(DateUtils.dateAdd(DateUtils.INTERVAL_YEAR,lastFinishTime,1))
  }
}
