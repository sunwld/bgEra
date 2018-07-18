package com.collie.bgEra.cloudApp.redisCache

import java.nio.charset.Charset

import com.collie.bgEra.cloudApp.utils.{ContextHolder, KryoUtil}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.stereotype.Component
import redis.clients.jedis.{JedisCluster, Tuple}
import java.{util => ju}

import com.collie.bgEra.cloudApp.CloudAppContext
import com.collie.bgEra.cloudApp.redisCache.bean.ZSetItemBean

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
  *
  */
@Component
class RedisService {

  private val jedis: JedisCluster = ContextHolder.getBean(classOf[CloudAppContext]).jedisCluster

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
    * @param key  zset key
    * @param item 要插入的项
    * @param expireTime  key超时时间， -1表示永不超时
    * @return  插入了多少项
    */
  def addZsetItem(key: String, item: ZSetItemBean, expireTime: Int = -1): Long={
    val result = jedis.zadd(key,item.score,item.id)
    if(expireTime > 0){
      jedis.expire(key,expireTime)
    }
    result
  }

  /**
    * 向zset中插入一项,然后对zset按照score进行修剪，保留指定secore区间（闭合区间）的项
    * @param key  zset key
    * @param item 插入的项
    * @param expireTime 超时时间，-1永不超时
    * @param minScore  要保留区间的score区间最小值
    * @param maxScore  要保留区间的score区间最大值
    * @return  _1 添加 的项的数量， 删除的项的数量
    */
  def addZsetItemAndTrimByScore(key: String, item: ZSetItemBean, expireTime: Int = -1,minScore: Double = Double.MinValue,maxScore:Double = Double.MaxValue)={
    val add = addZsetItem(key,item,expireTime)
    val delete = zsetTrimByScore(key,minScore,maxScore)
    (add,delete)
  }


  /**
    * 向zset中插入一项,然后对zset按照index进行修剪，只保留records项
    * @param key  zset key
    * @param item  要添加的项
    * @param records  要保留多少项,0表示不做修剪
    * @param expireTime  超时时间，-1永不超时
    * @param reverse  正序还是倒序，默认为false，正序
    * @return
    */
  def addZsetItemAndTrimByIndex(key: String, item: ZSetItemBean, records: Long = 0, expireTime: Int = -1,reverse:Boolean = false): Long={
    addZsetItem(key,item,expireTime)
    if(reverse){
      zsetRevTrimByIndex(key,0,records-1)
    }else{
      zsetTrimByIndex(key,0,records-1)
    }
  }

  /**
    * 向zset中插入多项
    * @param key zset key
    * @param items 插入的项
    * @param expireTime 超时时间，-1永不超时
    * @return  添加的项的数量
    */
  def addZsetItems(key: String, items: ju.List[ZSetItemBean], expireTime: Int = -1): Long ={
    val map: ju.Map[String,java.lang.Double] = new ju.HashMap()
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
    * 向zset中插入一项,然后对zset按照index进行修剪，只保留records项
    * @param key  zset key
    * @param item  要添加的项
    * @param records  要保留多少项
    * @param expireTime  超时时间，-1永不超时
    * @param reverse  正序还是倒序，默认为false，正序
    * @return
    */
  def addZsetItemsAndTrimByIndex(key: String, item: ju.List[ZSetItemBean], records: Long, expireTime: Int = -1,reverse:Boolean = false): Long={
    val addCount = addZsetItems(key,item,expireTime)
    var maxIndex: Long = records match {
      case i if i > 0 => i - 1
      case _ => -1
    }
    if(reverse){
      zsetRevTrimByIndex(key,0,maxIndex)
    }else{
      zsetTrimByIndex(key,0,maxIndex)
    }
    addCount
  }

  /**
    * 向zset中插入一项,然后对zset按照score进行修剪，保留指定secore区间（闭合区间）的项
    * @param key  zset key
    * @param item 插入的项
    * @param expireTime 超时时间，-1永不超时
    * @param minScore  要保留区间的score区间最小值
    * @param maxScore  要保留区间的score区间最大值
    * @return
    */
  def addZsetItemsAndTrimByScore(key: String, item: ju.List[ZSetItemBean], expireTime: Int = -1,minScore: Double,maxScore:Double): Long={
    addZsetItems(key,item,expireTime)
    zsetTrimByScore(key,minScore,maxScore)
  }

  /**
    * 根据score闭合区间，获取zset中的项,结果中包含score值(不删除这些项)
    * @return
    */
  def getZSetItemByScoreWithScore(key: String,minScore: Double, maxScore: Double): ju.List[ZSetItemBean] ={
    val items: ju.Set[Tuple] = jedis.zrangeByScoreWithScores(key,minScore,maxScore)
    var reusltList: ju.List[ZSetItemBean] = new ju.ArrayList()
    items.foreach(item => {
      reusltList.add(ZSetItemBean(item.getElement(),item.getScore()))
    })
    reusltList
  }

  /**
    * 删除给定的score闭合区间内zset的项
    */
  def delZSetItemByScore(key: String,minScore: Double, maxScore: Double): Long= {
    jedis.zremrangeByScore(key,minScore,maxScore)
  }

  def popZSetItemByScoreWithScore(key: String,minScore: Double, maxScore: Double): ju.List[ZSetItemBean] ={
    val result: ju.List[ZSetItemBean] = getZSetItemByScoreWithScore(key,minScore,maxScore)
    val delcount = delZSetItemByScore(key,minScore,maxScore)
    result
  }

  /**
    * 修剪zset，只保留指定分数区间（闭合区间）的元素，多余的元素将被删除
    */
  def zsetTrimByScore(key: String, minScore: Double = Double.MinValue, maxScore: Double = Double.MaxValue)= {
    jedis.zremrangeByScore(key,"-inf",s"($minScore")
    jedis.zremrangeByScore(key,s"($maxScore", "inf")
  }

  /**
    * 修剪zset，只保留指定index区间（闭合区间）的元素，多余的元素将被删除
    */
  def zsetTrimByIndex(key: String, minIndex: Long = 0, maxIndex: Long = -1): Long= {
    var delCount = 0L

    if(minIndex != 0) {
      delCount = jedis.zremrangeByRank(key, 0, minIndex - 1)
    }
    if (maxIndex != -1) {
      if(maxIndex > 0){
        delCount += jedis.zremrangeByRank(key, (maxIndex + 1 - delCount), -1)
      }else{
        delCount += jedis.zremrangeByRank(key, (maxIndex + 1), -1)
      }
    }

    delCount
  }

  /**
    * 倒序修剪zset，只保留指定index区间（闭合区间）的元素，多余的元素将被删除
    */
  def zsetRevTrimByIndex(key: String, minIndex: Long = 0, maxIndex: Long = -1): Long= {
    zsetTrimByIndex(key,~maxIndex,~minIndex)
  }

  def hsetPut(key: String, field: String, value: Any) : Long  ={
    jedis.hset(strSerializer.serialize(key),strSerializer.serialize(field),kryoUtil.writeClassAndObjectToByteArray(value))
  }

  def hsetPut(key: String, putMap: ju.Map[String,Any]) : String  ={
    val bytesPutMap: mutable.Map[Array[Byte], Array[Byte]] = putMap.map(i => {
      (strSerializer.serialize(i._1) , kryoUtil.writeClassAndObjectToByteArray(i._2))
    })
    jedis.hmset(strSerializer.serialize(key),bytesPutMap)
  }

  def hsetGet(key: String, field: String) : Any ={
    val k = strSerializer.serialize(key)
    val f = strSerializer.serialize(field)
    val bytes = jedis.hget(k,f)
    if(bytes != null){
      kryoUtil.readFromByteArray(bytes)
    }else{
      null
    }
  }

  def listLpop(lKey: String): Any = {
    val v: Array[Byte] = jedis.lpop(strSerializer.serialize(lKey))
    kryoUtil.readFromByteArray(v)
  }

  def listLpush(lKey: String,values: Any*) ={
    val k: Array[Byte] = strSerializer.serialize(lKey)
    val vs: Seq[Array[Byte]] = values.map(kryoUtil.writeClassAndObjectToByteArray(_))
    jedis.lpush(k,vs:_*)
  }

  def listRpush(lKey: String,value: Any) ={
    val k: Array[Byte] = strSerializer.serialize(lKey)
    val v = kryoUtil.writeClassAndObjectToByteArray(value)
    jedis.rpush(k,v)
  }

  def listSize(lKey: String): Long = {
    jedis.llen(strSerializer.serialize(lKey))
  }

  def hsetDelItem(key: String, field: String) = {
    jedis.hdel(strSerializer.serialize(key),strSerializer.serialize(field))
  }


  def delKey(key: String): Long = {
    jedis.del(strSerializer.serialize(key))
  }

  def delKeyByPattern(pattern: String) = {
    var delCount = 0L
    keys(pattern).foreach(x => {
      jedis.del(x)
      delCount += 1
    })
    delCount
  }

  def keys(pattern: String) ={
    val keys: mutable.Set[String] = mutable.Set()
    jedis.getClusterNodes().foreach(x => {
      keys.addAll(x._2.getResource().keys(pattern))
    })
    keys
  }

  def incr(key: String):Long = {
    jedis.incr(strSerializer.serialize(key))
  }

  def hasKey(key: String): Boolean = {
    jedis.exists(strSerializer.serialize(key))
  }
  def hexists(key: String,field: String): Boolean = {
    val k = strSerializer.serialize(key)
    val f = strSerializer.serialize(field)
    jedis.hexists(k,f)
  }
}
