package com.collie.bgEra.cloudApp.dtsf.mapper

import java.util

import com.collie.bgEra.cloudApp.dtsf.bean.TargetInfo
import com.collie.bgEra.cloudApp.redisCache.RedisObject
import org.apache.ibatis.session.{SqlSession, SqlSessionFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Repository

@Repository
class TaskMapper {
  @Autowired
  @Qualifier("mainSqlSessionFactory")
  private val factory: SqlSessionFactory = null

  private val NAMESPACE:String = "base.dtsfTest.mapper.DtsfMapper"

  @RedisObject(expireTime = -1)
  def qryAllTartInfo(redisKey: String) = {
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
}
