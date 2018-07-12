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

@Repository
class TaskMapper {
  @Autowired
  @Qualifier("mainSqlSessionFactory")
  private val factory: SqlSessionFactory = null

  private val NAMESPACE:String = "com.collie.bgEra.cloudApp.dtsf.mapper.TaskMapper"

  @EvictObject("'bgEra.cloudApp.dtsf.allZkSessionInfo'")
  def insertZkSessionInfo(zkSessionInfoList: util.List[ZkSessionInfo]) ={
    val insertSql = NAMESPACE+".insertZkSessionInfo"
    val deleteSql = NAMESPACE+".deleteAllZkSessionInfo"
    var session: SqlSession = null
    try{
      session = factory.openSession(false)
      session.delete(deleteSql)
      session.insert(insertSql,zkSessionInfoList)
      session.commit()
    }catch {
      case e:Exception => {
        session.rollback()
        e.printStackTrace()
        throw e
      }
    }finally {
      if(session != null){
        session.close()
      }
    }
  }

  @EvictObject("'bgEra.cloudApp.dtsf.allShardTargetInfo'")
  def insertTargetShardingMap(sharedTargetInfo: util.List[ShardingTarget])={
    val insertSql = NAMESPACE+".insertTargetShardingMap"
    val deleteSql = NAMESPACE+".deleteTargetShardingMap"
    var session: SqlSession = null
    try{
      session = factory.openSession(false)
      session.delete(deleteSql)
      session.insert(insertSql,sharedTargetInfo)
      session.commit()
    }catch {
      case e:Exception => {
        session.rollback()
        e.printStackTrace()
        throw e
      }
    } finally {
      if(session != null){
        session.close()
      }
    }
  }

  def qryAllTartInfo() = {
    val sql = NAMESPACE+".qryAllTartInfo"
    var session: SqlSession = null
    try{
      session = factory.openSession(false)
      val result: ju.List[TargetInfo] = session.selectList(sql)
      result
    }finally {
      if(session != null){
        session.close()
      }
    }
  }


  @HsetGetItem(cacheKey = "'bgEra.cloudApp.dtsf.allShardTargetInfo'", field = "#zkSessionId")
  def getTargetShardingBySessionId(zkSessionId: String)={
    val sql =  NAMESPACE+".getTargetShardingBySessionId"
    var session: SqlSession = null
    try{
      session = factory.openSession(false)
      val list: util.List[String] = session.selectList(sql,zkSessionId)
      list
    }finally {
      if(session != null){
        session.close()
      }
    }
  }

  @ZsetPopByScore(cacheKey = "'bgEra.dtsf.myTaskZset.'+#zkSessionId",maxScoreSpEl = "#now")
  def qryPerpredTaskListByTargets(zkSessionId: String,targetList: util.List[String],now: Double) : util.List[ZSetItemBean] ={
    val sql = NAMESPACE+".qryPerpredTaskListByTargets"
    var session: SqlSession = null
    var result : java.util.List[ZSetItemBean] = new util.ArrayList[ZSetItemBean]()
    try{
      session = factory.openSession(false)
      val list: util.List[TaskInfo] = session.selectList(sql,targetList)
      for(task <- list){
        result.add(ZSetItemBean(task.targetId + "||" + task.taskName,task.getNextTime().getTime()))
      }
      result
    }finally {
      if(session != null){
        session.close()
      }
    }
  }

  @HsetGetItem(cacheKey = "'bgEra.cloudApp.dtsf.TargetHset'",field = "#targetName")
  def qryTartInfoByName(targetName: String) = {
    val targetSql = NAMESPACE+".qryTartInfoByName"
    val resourceSql = NAMESPACE+".qryResourceMapByTarget"
    var session: SqlSession = null
    try{
      session = factory.openSession(false)
      val target: TargetInfo = session.selectOne(targetSql,targetName)
      val resouceList: util.List[ResourceInfo] = session.selectList(resourceSql,targetName)
      val resourceMap = mutable.Map[String,ResourceInfo]()
      resouceList.foreach(resourceInfo => resourceMap.put(resourceInfo.resourceName,resourceInfo))
      target.setResourceMap(resourceMap)
      target
    }finally {
      if(session != null){
        session.close()
      }
    }
  }

  @HsetGetItem(cacheKey = "'bgEra.cloudApp.dtsf.taskMap'",field = "#taskId")
  def qryTaskInfoById(taskId: String) ={
    val sql = NAMESPACE+".qryTaskInfoById"
    val taskUnitSql = NAMESPACE+".qryWorkUnitNameByTask"
    val paramMap: ju.Map[String,String] = new ju.HashMap()
    val p = taskId.split("\\|\\|")
    paramMap.put("targetId",p(0))
    paramMap.put("taskName",p(1))
    var session: SqlSession = null
    try{
      session = factory.openSession(false)
      val taskInfo: TaskInfo = session.selectOne(sql,paramMap)
      val taskUnitList: util.List[String] = session.selectList(taskUnitSql, paramMap)
      taskInfo.setWorkUnitList(taskUnitList)
      taskInfo
    }finally {
      if(session != null){
        session.close()
      }
    }
  }

  @HsetGetItem(cacheKey = "'bgEra.cloudApp.dtsf.workUnitMap'", field = "#workUnitId")
  def qryWorkUnitInfoById(workUnitId: String) ={
    val sql = NAMESPACE+".qryWorkUnitInfoById"
    val paramMap: ju.Map[String,String] = new ju.HashMap()
    val p = workUnitId.split("\\|\\|")
    paramMap.put("targetId",p(0))
    paramMap.put("taskName",p(1))
    paramMap.put("workUnitName",p(2))
    var session: SqlSession = null
    try{
      session = factory.openSession(false)
      val unit: WorkUnitInfo = session.selectOne(sql,paramMap)
      unit
    }finally {
      if(session != null){
        session.close()
      }
    }
  }

  @HsetPutItem(cacheKey = "'bgEra.cloudApp.dtsf.taskMap'",field = "#taskInfo.targetId+'||'+#taskInfo.taskName",hsetItemEl = "#taskInfo")
  def updateTaskInfo(taskInfo: TaskInfo) ={
    val sql = NAMESPACE+".updateTaskInfo"
    var session: SqlSession = null
    try {
      session = factory.openSession(false)
      session.update(sql, taskInfo)
      session.commit()
    } catch {
      case e:Exception => {
        session.rollback()
        e.printStackTrace()
        throw e
      }
    } finally {
      if(session != null){
        session.close()
      }
    }
  }

  @HsetPutItem(cacheKey = "'bgEra.cloudApp.dtsf.workUnitMap'",field = "#unit.targetId+'||'+#unit.taskName+'||'+#unit.workUnitName",hsetItemEl = "#unit")
  def updateWorkUnitInfo(unit: WorkUnitInfo) ={
    val sql = NAMESPACE+".updateWorkUnitInfo"
    var session: SqlSession = null
    try{
      session = factory.openSession(false)
      session.update(sql,unit)
      session.commit()
    }catch {
      case e:Exception => {
        session.rollback()
        e.printStackTrace()
        throw e
      }
    } finally {
      if(session != null){
        session.close()
      }
    }
  }

  @ZsetWeedoutByIndex(cacheKey = "'bgEra.dtsf.myTaskZset.'+#zkSessionId",addRecords = "#zsetList",keepRecords = -1)
  def giveBackTaskZsetList(zkSessionId: String, zsetList: ju.List[ZSetItemBean]): Unit ={

  }

  def saveDtfErrorLog(error: TaskErrorBean) ={
    val insertSql = NAMESPACE+".saveDtfErrorLog"
    var session: SqlSession = null
    try{
      session = factory.openSession(false)
      session.insert(insertSql,insertSql)
      session.commit()
    }catch {
      case e:Exception => {
        session.rollback()
        e.printStackTrace()
        throw e
      }
    }finally {
      if(session != null){
        session.close()
      }
    }
  }


}
