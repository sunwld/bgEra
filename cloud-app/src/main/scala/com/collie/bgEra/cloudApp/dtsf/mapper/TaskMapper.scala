package com.collie.bgEra.cloudApp.dtsf.mapper

import java.util

import com.collie.bgEra.cloudApp.dtsf.bean._
import com.collie.bgEra.cloudApp.redisCache.bean.ZSetItemBean
import com.collie.bgEra.cloudApp.redisCache._
import org.apache.ibatis.session.{SqlSession, SqlSessionFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Repository

import scala.collection.JavaConversions._
import scala.collection.mutable

@Repository
class TaskMapper {
  @Autowired
  @Qualifier("mainSqlSessionFactory")
  private val factory: SqlSessionFactory = null

  private val NAMESPACE:String = "base.dtsfTest.mapper.DtsfMapper"

  @GetHsetItem(cacheKey = "'bgEra.cloudApp.dtsf.TargetHset'",field = "#targetName")
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

//  @CacheObject(expireTime = -1,cacheKey = "'bgEra.cloudApp.dtsf.myTargetMap.'+#zkSessionId")
  def qryTaskInfoById(zkSessionId: String,targetList: util.List[String]) : util.Map[String,TaskInfo] ={
    val sql = NAMESPACE+".qryTaskInfoById"
    var session: SqlSession = null
    var resultMap : java.util.Map[String,TaskInfo] = new util.HashMap()
    try{
      session = factory.openSession(false)
      val list: util.List[TaskInfo] = session.selectList(sql,targetList)
      for(task <- list){
        resultMap.put(task.getTaskName, task)
      }
      resultMap
    }finally {
      if(session != null){
        session.close()
      }
    }
  }

  @CacheObject(expireTime = -1, cacheKey = "'bgEra.cloudApp.dtsf.allShardTargetInfo'")
  def getTargetShardingMap()={
    //select zksession,dtftargetid from dtf_sharding_map
    val sql =  NAMESPACE+".getTargetShardingMap"
    var session: SqlSession = null
    var resultMap: java.util.Map[String,util.List[String]] = new util.HashMap()
    try{
      session = factory.openSession(false)
      val list: util.List[ShardingTarget] = session.selectList(sql)
      for(target <- list ){
        var targetListBySession:util.List[String] = resultMap.get(target.getZkSessionId())
        if (targetListBySession == null){
          targetListBySession = new util.ArrayList[String]()
          resultMap.put(target.getZkSessionId,targetListBySession)
        }
        targetListBySession.add(target.getDtfTargetId)
      }
      resultMap
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
    }finally {
      session.commit()
      if(session != null){
        session.close()
      }
    }
  }

  @EvictObject("'bgEra.cloudApp.dtsf.allZkSessionInfo'")
  def insertZkSessionInfo(zkSessionInfoList: util.List[ZkSessionInfo]) ={
    val insertSql = NAMESPACE+".insertZkSessionInfo"
    val deleteSql = NAMESPACE+".deleteAllZkSessionInfo"
    var session: SqlSession = null
    try{
      session = factory.openSession(false)
      session.delete(deleteSql)
      session.insert(insertSql,zkSessionInfoList)
    }finally {
      session.commit()
      if(session != null){
        session.close()
      }
    }
  }

//  @EvictObject("'bgEra.dtsf.myTargetMap.'+#zkSessionId")
//  def evictTaskInfoListBySession(zkSessionId: String)={}

  @PopZsetByScore(cacheKey = "'bgEra.dtsf.myTaskZset.'+#zkSessionId",maxScoreSpEl = "#now")
  def qryPerpredTaskListByTargets(zkSessionId: String,targetList: util.List[String],now: Double) : util.List[ZSetItemBean] ={
    val sql = NAMESPACE+".qryPerpredTaskListByTargets"
    var session: SqlSession = null
    var result : java.util.List[ZSetItemBean] = new util.ArrayList[ZSetItemBean]()
    try{
      session = factory.openSession(false)
      val list: util.List[TaskInfo] = session.selectList(sql,targetList)
      for(task <- list){
        result.add(ZSetItemBean(task.getTargetId + "." + task.getTaskName,task.getNextTime.getTime))
      }
      result
    }finally {
      if(session != null){
        session.close()
      }
    }
  }
}
