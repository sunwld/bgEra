package com.collie.bgEra.cloudApp.dtsf.mapper

import java.util

import com.collie.bgEra.cloudApp.dtsf.bean.{ShardingTarget, TargetInfo, TaskInfo, ZkSessionInfo}
import com.collie.bgEra.cloudApp.redisCache.{CacheObject, EvictObject}
import org.apache.ibatis.session.{SqlSession, SqlSessionFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Repository

import scala.collection.JavaConversions._

@Repository
class TaskMapper {
  @Autowired
  @Qualifier("mainSqlSessionFactory")
  private val factory: SqlSessionFactory = null

  private val NAMESPACE:String = "base.dtsfTest.mapper.DtsfMapper"

  @CacheObject(expireTime = -1,cacheKey= "'bgEra.dtsf.allTartInfo'")
  def qryAllTartInfo() = {
    val sql = NAMESPACE+".qryAllTartInfo"
    var session: SqlSession = null
    try{
      session = factory.openSession(false)
      val list: util.List[TargetInfo] = session.selectList(sql)
      list
    }finally {
      if(session != null){
        session.close()
      }
    }
  }

  @CacheObject(expireTime = -1, cacheKey = "'bgEra.dtsf.allShardTargetInfo'")
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

  @EvictObject("'bgEra.dtsf.allShardTargetInfo'")
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

  @EvictObject("'bgEra.dtsf.allZkSessionInfo'")
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

  @EvictObject("'bgEra.dtsf.myTargetMap.'+#zkSessionId")
  def evictTaskInfoListBySession(zkSessionId: String)={}

  @CacheObject(expireTime = 3600,cacheKey = "'bgEra.dtsf.myTargetMap.'+#zkSessionId")
  def qryTaskInfoListByTargets(zkSessionId: String,targetList: util.List[String]) : util.Map[String,TaskInfo] ={
    val sql = NAMESPACE+".qryTaskInfoListByTargets"
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
}
