package com.collie.bgEra.cloudApp.dtsf.mapper

import java.util
import java.util.Properties

import com.collie.bgEra.cloudApp.dtsf.bean._
import com.collie.bgEra.cloudApp.redisCache.bean.ZSetItemBean
import com.collie.bgEra.cloudApp.redisCache._
import org.apache.ibatis.session.{SqlSession, SqlSessionFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Repository
import java.{util => ju}

import com.collie.bgEra.cloudApp.kryoUtil.KryoUtil
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._
import scala.collection.mutable

@Repository
class TaskMapper {
  private val logger: Logger = LoggerFactory.getLogger("dtsf")

  @Autowired
  @Qualifier("bgEra_dtsf_SqlSessionFactory")
  private val factory: SqlSessionFactory = null

  @Autowired
  val redisService: RedisService = null

  private val kryoUtil: KryoUtil.type = KryoUtil

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

      val paramMap:util.Map[String,String] = new ju.HashMap[String,String]()
      paramMap.put("taskName",taskInfo.taskName)
      paramMap.put("targetId",taskInfo.targetId)
      val taskUnitList: util.List[String] = session.selectList(taskUnitSql, paramMap)
      taskInfo.setWorkUnitList(taskUnitList)
      taskInfo
    } finally {
      if (session != null) {
        session.close()
      }
    }
  }

  @HsetGetItem(cacheKey = "'bgEra.cloudApp.dtsf.workUnitMap'", field = "#workUnitId")
  def qryWorkUnitInfoById(workUnitId: String) = {
    val sql = NAMESPACE + ".qryWorkUnitInfoById"
    var session: SqlSession = null
    try {
      session = factory.openSession(false)
      val unit: WorkUnitInfo = session.selectOne(sql, workUnitId)
      unit
    } finally {
      if (session != null) {
        session.close()
      }
    }
  }

  @HsetPutItem(cacheKey = "'bgEra.cloudApp.dtsf.taskMap'", field = "#taskInfo.targetId+'||'+#taskInfo.taskName", hsetItemEl = "#taskInfo")
  def updateTaskInfo(taskInfo: TaskInfo) = {
  }

  @HsetPutItem(cacheKey = "'bgEra.cloudApp.dtsf.workUnitMap'", field = "#unit.targetId+'||'+#unit.taskName+'||'+#unit.workUnitName", hsetItemEl = "#unit")
  def updateWorkUnitInfo(unit: WorkUnitInfo) = {
  }

  @ZsetWeedoutByIndex(cacheKey = "'bgEra.cloudApp.dtsf.myTaskZset.'+#zkSessionId", addRecords = "#zsetList", keepRecords = -1)
  def giveBackTaskZsetList(zkSessionId: String, zsetList: ju.List[ZSetItemBean]): Unit = {}

  def flushTaskStatusToDB(zkSessionId: String): Unit ={
    val begin = System.currentTimeMillis()
    val targetList: util.List[String] = getTargetShardingBySessionId(zkSessionId)
    if(targetList == null || targetList.isEmpty){
      return
    }
    val allTask: util.Map[String, Array[Byte]] = redisService.hsetGetAllNoVlueDeser("bgEra.cloudApp.dtsf.taskMap")
    val allWorkUnit: util.Map[String, Array[Byte]] = redisService.hsetGetAllNoVlueDeser("bgEra.cloudApp.dtsf.workUnitMap")
    val myTaskIds = qryMyTaskIdListByTargets(zkSessionId,targetList)
    val myWorkUnitIds = qryMyWorkUnitIdListByTargets(zkSessionId,targetList)
    logger.trace(s"flushTaskStatusToDB  get task and workunit infos times : ${System.currentTimeMillis() - begin}")
    if(allTask == null || allWorkUnit == null || myTaskIds == null || myWorkUnitIds == null ||
        allTask.isEmpty() || allWorkUnit.isEmpty() || myTaskIds.isEmpty() || myWorkUnitIds.isEmpty()){
      return
    }
    val taskSql = NAMESPACE + ".updateTaskInfo"
    val workUnitSql = NAMESPACE + ".updateWorkUnitInfo"
    var session: SqlSession = null
    try {
      session = factory.openSession(false)

      myTaskIds.foreach(id => {
        val t = allTask.get(id)
        if(t!=null){
          session.update(taskSql,kryoUtil.readFromByteArray(t))
        }
      })
      session.commit()
      myWorkUnitIds.foreach(id => {
        val w = allWorkUnit.get(id)
        if(w != null){
          session.update(workUnitSql,kryoUtil.readFromByteArray(w))
        }
      })
      session.commit()

      logger.trace(s"flushTaskStatusToDB total times : ${System.currentTimeMillis() - begin}")
    } finally {
      if (session != null) {
        session.close()
      }
    }
  }

  @CacheObject(expireTime = -1,cacheKey = "'bgEra.cloudApp.dtsf.myTaskIds.'+#zkSessionId")
  private def qryMyTaskIdListByTargets(zkSessionId: String, targetList: util.List[String]): util.List[String] = {
    val sql = NAMESPACE + ".qryMyTaskIdListByTargets"
    var session: SqlSession = null
    var result: java.util.List[String] = new util.ArrayList()
    try {
      session = factory.openSession(false)
      result = session.selectList(sql, targetList)
      result
    } finally {
      if (session != null) {
        session.close()
      }
    }
  }

  @CacheObject(expireTime = -1,cacheKey = "'bgEra.cloudApp.dtsf.myWorkUnitIds.'+#zkSessionId")
  private def qryMyWorkUnitIdListByTargets(zkSessionId: String, targetList: util.List[String]): util.List[String] = {
    val sql = NAMESPACE + ".qryMyWorkUnitIdListByTargets"
    var session: SqlSession = null
    var result: java.util.List[String] = new util.ArrayList()
    try {
      session = factory.openSession(false)
      result = session.selectList(sql, targetList)
      result
    } finally {
      if (session != null) {
        session.close()
      }
    }
  }

  def saveDtfErrorLog(error: TaskErrorBean) = {
    val sql = NAMESPACE + ".saveDtfErrorLog"
    var session: SqlSession = null
    var result = 0
    try {
      session = factory.openSession(false)
      result = session.insert(sql,error)
      session.commit()
      result
    } finally {
      if (session != null) {
        session.close()
      }
    }
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
      zkSessionIdList.foreach(sessionId => {
        redisService.delKey(s"bgEra.cloudApp.dtsf.myTaskZset.${sessionId}")
        redisService.delKey(s"bgEra.cloudApp.dtsf.myTaskIds.${sessionId}")
        redisService.delKey(s"bgEra.cloudApp.dtsf.myWorkUnitIds.${sessionId}")
      })
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

  def qryResourcePropByName(name: String) = {
    val connPros = new Properties()

    connPros.setProperty("druid.driverClassName","com.mysql.jdbc.Driver")
    connPros.setProperty("druid.username","dtsf")
    connPros.setProperty("druid.password","1234yjd")
    connPros.setProperty("druid.url","jdbc:mysql://133.96.6.1:3306/dtsfdb?characterEncoding=utf-8&useSSL=false")

    connPros.setProperty("druid.initialSize","30")
    connPros.setProperty("druid.minIdle","30")
    connPros.setProperty("druid.maxActive","100")
    connPros.setProperty("druid.poolPreparedStatements","false")
    connPros.setProperty("druid.maxPoolPreparedStatementPerConnectionSize","0")
    connPros
  }



  def qryAllTartInfoTest(factory1: SqlSessionFactory) = {
    val sql = NAMESPACE + ".qryAllTartInfo"
    var session: SqlSession = null
    try {
      session = factory1.openSession(false)
      val result: ju.List[TargetInfo] = session.selectList(sql)
      result
    } finally {
      if (session != null) {
        session.close()
      }
    }
  }


}
