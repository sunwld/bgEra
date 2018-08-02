package com.collie.bgEra.hpdc.service

import java.util.Date
import com.collie.bgEra.commons.util.{DateUtils, SerialNumberUtils}
import com.collie.bgEra.hpdc.service.bean.CalculateIncacheStatsValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import scala.collection.JavaConversions._

@Service
class StatisticsCalculateIncacheService {

  @Autowired
  private val statisticsCalculateIncacheMapper: StatisticsCalculateIncacheMapper = null

  /**
    *
    * @param shellName
    * @param targetId
    * @param valueItems
    * @return (Long,Doubel): (lastSnapId, SnapIdDiff,StatsValueDiff), snapIdDiff: the seconds of currentSnapId - lastSnapId
    *         note: can return A negative number（-999）
    */
  def calculateDiff2LastValue[T](shellName: String, targetId: String, valueItems: CalculateIncacheStatsValue[T]): java.util.Map[T, (String, Long, Double)] = {
    var lastValMap: CalculateIncacheStatsValue[T] = statisticsCalculateIncacheMapper.getLastStatisticsValue[T](shellName, targetId)
    try {
      val resultMap: java.util.Map[T, (String, Long, Double)] = new java.util.HashMap[T, (String, Long, Double)]()
      if (lastValMap.lastSnapId == null || "".equals(lastValMap.lastSnapId) || lastValMap.statsVal == null ||
        lastValMap.statsVal.isEmpty() || valueItems == null || valueItems.lastSnapId == null || valueItems.statsVal == null) {
        lastValMap = valueItems
        resultMap
      } else {
        valueItems.statsVal.foreach(cs => {
          //snapid, value
          val cv: (String, Double) = cs._2
          val lv: (String, Double) = lastValMap.statsVal.get(cs._1)

          if (lv != null) {
            // (String, Double)
            val lastDate: Date = SerialNumberUtils.getDateTimeBySerial(lv._1)
            val currDate: Date = SerialNumberUtils.getDateTimeBySerial(cv._1)
            resultMap.put(cs._1, (lastValMap.lastSnapId, DateUtils.dateDiff(DateUtils.INTERVAL_SECOND, lastDate, currDate), cv._2 - lv._2))
          }
          lastValMap.statsVal.put(cs._1, cv)
        })

        lastValMap.lastSnapId = valueItems.lastSnapId
        resultMap
      }
    } finally {
      statisticsCalculateIncacheMapper.putLastStatisticsValue(shellName, targetId, lastValMap)
    }
  }
}
