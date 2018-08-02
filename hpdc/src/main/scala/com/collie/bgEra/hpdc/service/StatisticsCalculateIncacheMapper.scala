package com.collie.bgEra.hpdc.service

import com.collie.bgEra.cloudApp.redisCache.{HsetGetItem, HsetPutItem}
import com.collie.bgEra.hpdc.service.bean.CalculateIncacheStatsValue
import org.springframework.stereotype.Repository

import scala.collection.mutable

@Repository
class StatisticsCalculateIncacheMapper {

  @HsetGetItem(cacheKey = "'bgEra.hdpc.ShellStatsValus'", field = "#shellName+'||'+#targetId")
  def getLastStatisticsValue[T](shellName: String, targetId: String): CalculateIncacheStatsValue[T] = {
    new CalculateIncacheStatsValue("", new java.util.HashMap[T, (String, Double)]())
  }

  @HsetPutItem(cacheKey = "'bgEra.hdpc.ShellStatsValus'", field = "#shellName+'||'+#targetId", hsetItemEl = "#statsValue")
  def putLastStatisticsValue[T](shellName: String, targetId: String, statsValue: CalculateIncacheStatsValue[T]): Unit = {}
}
