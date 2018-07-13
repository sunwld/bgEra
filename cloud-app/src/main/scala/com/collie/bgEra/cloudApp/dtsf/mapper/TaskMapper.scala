package com.collie.bgEra.cloudApp.dtsf.mapper

import java.util

import com.collie.bgEra.cloudApp.dtsf.bean._
import com.collie.bgEra.cloudApp.redisCache.bean.ZSetItemBean
import com.collie.bgEra.cloudApp.redisCache._
import org.apache.ibatis.session.{SqlSession, SqlSessionFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Repository
import java.{util => ju}

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

@Repository
class TaskMapper {
  @Autowired
  @Qualifier("bgEra_dtsf_SqlSessionFactory")
  private val factory: SqlSessionFactory = null

  @Autowired
  val redisService: RedisService = null

  private val NAMESPACE: String = "com.collie.bgEra.cloudApp.dtsf.mapper.TaskMapper"

  @EvictObject("'bgEra.cloudApp.dtsf.allZkSessionInfo'")
  def insertZkSessionInfo(zkSessionInfoList: util.List[ZkSessionInfo]) = {
    val insertSql = NAMESPACE + ".insertZkSessionInfo"
    val deleteSql = NAMESPACE + ".deleteAllZkSessionInfo"
    var session: SqlSession = null
    try {
      session = factory.openSession(false)
      session.delete(deleteSql)
      session.insert(insertSql, zkSessionInfoList)
      session.commit()
    } catch {
      case e: Exception => {
        session.rollback()
        throw e
      }
    } finally {
      if (session != null) {
        session.close()
      }
    }
  }

  @EvictObject("'bgEra.cloudApp.dtsf.allShardTargetInfo'")
  def insertTargetShardingMap(sharedTargetInfo: util.List[ShardingTarget]) = {
    val insertSql = NAMESPACE + ".insertTargetShardingMap"
    val deleteSql = NAMESPACE + ".deleteTargetShardingMap"
    var session: SqlSession = null
    try {
      session = factory.openSession(false)
      session.delete(deleteSql)
      session.insert(insertSql, sharedTargetInfo)
      session.commit()
    } catch {
      case e: Exception => {
        session.rollback()
        throw e
      }
    } finally {
      if (session != null) {
        session.close()
      }
    }
  }

  def qryAllTartInfo() = {
    val sql = NAMESPACE + ".qryAllTartInfo"
    var session: SqlSession = null
    try {
      session = factory.openSession(false)
      val result: ju.List[TargetInfo] = session.selectList(sql)
      result
    } finally {
      if (session != null) {
        session.close()
      }
    }
  }


  @HsetGetItem(cacheKey = "'bgEra.cloudApp.dtsf.allShardTargetInfo'", field = "#zkSessionId")
  def getTargetShardingBySessionId(zkSessionId: String) = {
    val sql = NAMESPACE + ".getTargetShardingBySessionId"
    var session: SqlSession = null
    try {
      session = factory.openSession(false)
      val list: util.List[String] = session.selectList(sql, zkSessionId)
      list
    } finally {
      if (session != null) {
        session.close()
      }
    }
  }

  @ZsetPopByScore(cacheKey = "'bgEra.cloudApp.dtsf.myTaskZset.'+#zkSessionId", maxScoreSpEl = "#now")
  def qryPerpredTaskListByTargets(zkSessionId: String, targetList: util.List[String], now: Double): util.List[ZSetItemBean] = {
    val sql = NAMESPACE + ".qryPerpredTaskListByTargets"
    var session: SqlSession = null
    var result: java.util.List[ZSetItemBean] = new util.ArrayList[ZSetItemBean]()
    try {
      session = factory.openSession(false)
      val list: util.List[TaskInfo] = session.selectList(sql, targetList)
      for (task <- list) {
        result.add(ZSetItemBean(task.taskId, task.getNextTime().getTime()))
      }
      result
    } finally {
      if (session != null) {
        session.close()
      }
    }
  }

  @HsetGetItem(cacheKey = "'bgEra.cloudApp.dtsf.TargetHset'", field = "#targetName")
  def qryTargetInfoByName(targetName: String) = {
    val targetSql = NAMESPACE + ".qryTargetInfoByName"
    val resourceSql = NAMESPACE + ".qryResourceMapByTarget"
    var session: SqlSession = null
    try {
      session = factory.openSession(false)
      val target: TargetInfo = session.selectOne(targetSql, targetName)
      val resouceList: util.List[ResourceInfo] = session.selectList(resourceSql, targetName)
      val resourceMap = mutable.Map[String, ResourceInfo]()
      resouceList.foreach(resourceInfo => resourceMap.put(resourceInfo.resourceName, resourceInfo))
      target.setResourceMap(resourceMap)
      target
    } finally {
      if (session != null) {
        session.close()
      }
    }
  }

  @HsetGetItem(cacheKey = "'bgEra.cloudApp.dtsf.taskMap'", field = "#taskId")
  def qryTaskInfoById(taskId: String) = {
    val sql = NAMESPACE + ".qryTaskInfoById"
    val taskUnitSql = NAMESPACE + ".qryWorkUnitIdByTask"
    var session: SqlSession = null
    try {
      session = factory.openSession(false)
      val taskInfo: TaskInfo = session.selectOne(sql, taskId)
      val taskUnitList: util.List[String] = session.selectList(taskUnitSql, taskId)
      taskInfo.setWorkUnitList(taskUnitList)
      taskInfo
    } finally {
      if (session != null) {
        session.close()
      }
    }
  }

  @HsetGetItem(cacheKey = "'bgEra.cloudApp.dtsf.workUnitMap'", field = "#workUnitId")
  def qryWorkUnitInfoById(workUnitId: String, session: SqlSession) = {
    val sql = NAMESPACE + ".qryWorkUnitInfoById"
    val unit: WorkUnitInfo = session.selectOne(sql, workUnitId)
    unit
  }

  @HsetPutItem(cacheKey = "'bgEra.cloudApp.dtsf.taskMap'", field = "#taskInfo.targetId+'||'+#taskInfo.taskName", hsetItemEl = "#taskInfo")
  def updateTaskInfo(taskInfo: TaskInfo, session: SqlSession) = {
    val sql = NAMESPACE + ".updateTaskInfo"
    session.update(sql, taskInfo)
    session.commit()
  }

  @HsetPutItem(cacheKey = "'bgEra.cloudApp.dtsf.workUnitMap'", field = "#unit.targetId+'||'+#unit.taskName+'||'+#unit.workUnitName", hsetItemEl = "#unit")
  def updateWorkUnitInfo(unit: WorkUnitInfo, session: SqlSession) = {
    val sql = NAMESPACE + ".updateWorkUnitInfo"
    session.update(sql, unit)
    session.commit()
  }

  @ZsetWeedoutByIndex(cacheKey = "'bgEra.cloudApp.dtsf.myTaskZset.'+#zkSessionId", addRecords = "#zsetList", keepRecords = -1)
  def giveBackTaskZsetList(zkSessionId: String, zsetList: ju.List[ZSetItemBean]): Unit = {}

  def saveDtfErrorLog(error: TaskErrorBean) = {
    val insertSql = NAMESPACE + ".saveDtfErrorLog"
    var session: SqlSession = null
    try {
      session = factory.openSession(false)
      session.insert(insertSql, error)
      session.commit()
    } catch {
      case e: Exception => {
        session.rollback()
        throw e
      }
    } finally {
      if (session != null) {
        session.close()
      }
    }
  }

  def saveDtfErrorLog(error: TaskErrorBean, session: SqlSession) = {
    val insertSql = NAMESPACE + ".saveDtfErrorLog"
    session.insert(insertSql, error)
    session.commit()
  }

  def flushDtsfRedisCache(): Unit = {
    val sql = NAMESPACE + ".qryAllZkSessionId"
    var session: SqlSession = null
    try {
      session = factory.openSession(false)
      val keys: Array[String] = Array("bgEra.cloudApp.dtsf.allZkSessionInfo",
        "bgEra.cloudApp.dtsf.allShardTargetInfo", "bgEra.cloudApp.dtsf.TargetHset",
        "bgEra.cloudApp.dtsf.taskMap", "bgEra.cloudApp.dtsf.workUnitMap")
      val zkSessionIdList: ju.List[String] = session.selectList(sql)
      zkSessionIdList.foreach(sessionId => redisService.delKey(s"bgEra.cloudApp.dtsf.myTaskZset.${sessionId}"))
      keys.foreach(redisService.delKey(_))
    } catch {
      case e: Exception => {
        throw e
      }
    } finally {
      if (session != null) {
        session.close()
      }
    }
  }


}
