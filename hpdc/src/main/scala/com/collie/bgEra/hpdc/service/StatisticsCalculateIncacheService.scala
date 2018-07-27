package com.collie.bgEra.hpdc.service

import java.util.Date

import com.collie.bgEra.cloudApp.redisCache.{HsetGetItem, HsetPutItem}
import com.collie.bgEra.commons.util.{DateUtils, SerialNumberUtils}
import com.collie.bgEra.hpdc.service.bean.CalculateIncacheStatsValue
import org.springframework.stereotype.Service

import scala.collection.mutable

@Service
class StatisticsCalculateIncacheService[T] {
  @HsetGetItem(cacheKey = "'bgEra.hdpc.ShellStatsValus'", field = "#shellName+'||'+#targetId")
  def getLastStatisticsValue(shellName: String, targetId: String): CalculateIncacheStatsValue[T] = {
    new CalculateIncacheStatsValue("", mutable.Map[T, (String, Double)]())
  }

  @HsetPutItem(cacheKey = "'bgEra.hdpc.ShellStatsValus'", field = "#shellName+'||'+#targetId+'||'+#statsIndx", hsetItemEl = "#valueItem")
  def putLastStatisticsValue(shellName: String, targetId: String, statsResult: CalculateIncacheStatsValue[T]): Unit = {}

  /**
    *
    * @param shellName
    * @param targetId
    * @param valueItems
    * @return (Long,Doubel): (lastSnapId, SnapIdDiff,StatsValueDiff), snapIdDiff: the seconds of currentSnapId - lastSnapId
    *         note: can return A negative number（-999）
    */
  def calculateDiff2LastValue(shellName: String, targetId: String, valueItems: CalculateIncacheStatsValue[T]): mutable.Map[T, (String, Long, Double)] = {
    val lastValMap = getLastStatisticsValue(shellName, targetId)
    try {
      val resultMap: mutable.Map[T, (String, Long, Double)] = mutable.HashMap[T, (String, Long, Double)]()
      if (lastValMap.lastSnapId == null || "".equals(lastValMap.lastSnapId) || lastValMap.statsVal == null
        || valueItems == null || valueItems.lastSnapId == null || valueItems.statsVal == null) {
        mutable.HashMap[T, (String, Long, Double)]()
      } else {
        val curStatsVal: mutable.Map[T, (String, Double)] = valueItems.statsVal
        //cs:(String, (String, Double))
        curStatsVal.foreach(cs => {
          //snapid, value
          val cv: (String, Double) = cs._2
          val lv: (String, Double) = lastValMap.statsVal(cs._1)

          // (String, Double)
          val lastDate: Date = SerialNumberUtils.getDateTimeBySerial(lv._1)
          val currDate: Date = SerialNumberUtils.getDateTimeBySerial(cv._1)
          resultMap.put(cs._1, (lastValMap.lastSnapId, DateUtils.dateDiff(DateUtils.INTERVAL_SECOND, lastDate, currDate), cv._2 - lv._2))
          lastValMap.statsVal.put(cs._1, cv)
        })

        lastValMap.lastSnapId = valueItems.lastSnapId
        resultMap
      }
    } finally {
      putLastStatisticsValue(shellName, targetId, lastValMap)
    }
  }
}
