package com.collie.bgEra.cloudApp.redisCache

import java.nio.charset.Charset

import com.collie.bgEra.cloudApp.utils.KryoUtil
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.stereotype.Component
import redis.clients.jedis.{JedisCluster, Tuple}
import java.util

import com.collie.bgEra.cloudApp.redisCache.bean.ZSetItemBean

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  *
  */
@Component
class RedisService {
  @Autowired
  @Qualifier("jedisCluster")
  private val jedis: JedisCluster = null

  private val strSerializer: StringRedisSerializer = new StringRedisSerializer(Charset.forName("UTF8"))

  private val kryoUtil: KryoUtil.type = KryoUtil

  /**
    * 从redis中获取数据
    * @param key key
    * @return
    */
  def getObject(key: String): Any = {
    if(key == null){
      null
    }else{
      val k:Array[Byte] = strSerializer.serialize(key)
      val bytes = jedis.get(k)
      if(bytes == null){
        null
      }else{
        kryoUtil.readFromByteArray(bytes)
      }
    }
  }

  private def doSetObject(key: Array[Byte], value: Array[Byte], expireTime: Int): String ={
    if(expireTime <= 0){
      jedis.set(key,value)
    }else{
      if (jedis.exists(key)) {
        jedis.set(key, value, "xx".getBytes, "ex".getBytes, expireTime)
      } else {
        jedis.set(key, value, "nx".getBytes, "ex".getBytes, expireTime)
      }

    }
  }

  /**
    *
    * @param key key
    * @param value value
    * @param expireTime  过期时间，单位秒
    * @return
    */
  def setObject(key: String, value: Any, expireTime: Int): String ={
    val k:Array[Byte] = strSerializer.serialize(key)
    var v:Array[Byte] = null
    value match {
      case a: Array[Byte] => v = a
      case _ => v = kryoUtil.writeClassAndObjectToByteArray(value)
    }
    doSetObject(k,v,expireTime)
  }

  /**
    * 向zset中插入一项
    * @param key
    * @param item
    * @param expireTime
    * @return
    */
  def addZsetItem(key: String, item: ZSetItemBean, expireTime: Int): Long={
    val result = jedis.zadd(key,item.score,item.id)
    if(expireTime > 0){
      jedis.expire(key,expireTime)
    }
    result
  }

  /**
    * 向zset中插入一项,并且永不超时
    * @param key
    * @param item
    * @return
    */
  def addZsetItem(key: String, item: ZSetItemBean): Long={
    addZsetItem(key,item,-1)
  }

  /**
    * 向zset中插入多项
    * @param key
    * @param items
    * @param expireTime
    * @return
    */
  def addZsetItem(key: String, items: mutable.Seq[ZSetItemBean], expireTime: Int): Long ={
    val map: mutable.HashMap[String,java.lang.Double] = mutable.HashMap()
    items.foreach(item => {
      map.put(item.id,item.score)
    })
    val result = jedis.zadd(key,map)
    if(expireTime > 0){
      jedis.expire(key,expireTime)
    }
    result
  }

  /**
    * 向zset中插入多项,并且永不超时
    * @param key
    * @param items
    * @return
    */
  def addZsetItem(key: String, items: mutable.Seq[ZSetItemBean]): Long={
    addZsetItem(key,items,-1)
  }

  /**
    * 根据score闭合区间，获取zset中的项,结果中包含score值(不删除这些项)
    * @param key
    * @param minScore
    * @param maxScore
    * @return
    */
  def getZSetItemByScoreWithScore(key: String,minScore: Double, maxScore: Double): mutable.Seq[ZSetItemBean] ={
    val items: util.Set[Tuple] = jedis.zrangeByScoreWithScores(key,minScore,maxScore)
    var reusltList: ListBuffer[ZSetItemBean] = ListBuffer()
    items.foreach(item => {
      reusltList.append(ZSetItemBean(item.getElement,item.getScore()))
    })
    reusltList
  }

  /**
    * 删除给定的score闭合区间内zset的项
    * @param key
    * @param minScore
    * @param maxScore
    */
  def delZSetItemByScore(key: String,minScore: Double, maxScore: Double): Long= {
    jedis.zremrangeByScore(key,minScore,maxScore)
  }

  def popZSetItemByScoreWithScore(key: String,minScore: Double, maxScore: Double): mutable.Seq[ZSetItemBean] ={
    val result: mutable.Seq[ZSetItemBean] = getZSetItemByScoreWithScore(key,minScore,maxScore)
    delZSetItemByScore(key,minScore,maxScore)
    result
  }

//  /**
//    * 修剪zset，只保留指定个数的元素
//    * @param maxLength  只保留此参数指定的个数的元素数量，多余的元素将被删除
//    * @param asc 是否升序，默认为true
//    */
//  def zsetTrimByIndex(key: String, maxLength: Int, asc:Boolean = true)= {
//    jedis.zrem
//  }
//
//  /**
//    * 修剪zset，只保留指定分数区间（闭合区间）的元素，多余的元素将被删除
//    * @param minScore
//    * @param maxScore
//    */
//  def zsetTrimByScore(key: String, minScore: Double, maxScore: Double)= {
//
//  }


  def del(key: String): Long = {
    jedis.del(strSerializer.serialize(key))
  }

  def incr(key: String):Long = {
    jedis.incr(strSerializer.serialize(key))
  }

  def hasKey(key: String): Boolean = {
    jedis.exists(strSerializer.serialize(key))
  }
}
