package com.collie.bgEra.cloudApp.redisCache

import com.collie.bgEra.cloudApp.utils.KryoUtil
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Component
import redis.clients.jedis.JedisCluster

/**
  *
  */
@Component
class RedisService {
  @Autowired
  @Qualifier("jedisCluster")
  private val jedis: JedisCluster = null

  private val kryoUtil: KryoUtil.type = KryoUtil

  /**
    * 从redis中获取数据
    * @param key
    * @return
    */
  def getObject(key: Any): Any = {
    var k:Array[Byte] = null
    if(key.isInstanceOf[Array[Byte]]){
      k = key.asInstanceOf[Array[Byte]]
    }else{
      k = kryoUtil.writeClassAndObjectToByteArray(key)
    }
    val bytes = jedis.get(k)
    if(bytes == null){
      null
    }else{
      kryoUtil.readFromByteArray(bytes)
    }
  }

  /**
    *
    * @param key
    * @param value
    * @param expireTime
    * @return
    */
  def setObject(key: Array[Byte], value: Array[Byte], expireTime: Long): String ={
    if(expireTime <= 0){
      jedis.set(key,value)
    }else{
      if(jedis.exists(key)){
        jedis.set(key,value,"xx".getBytes,"ex".getBytes,expireTime)
      }else{
        jedis.set(key,value,"nx".getBytes,"ex".getBytes,expireTime)
      }

    }
  }

  /**
    *
    * @param key
    * @param value
    * @param expireTime
    * @return
    */
  def setObject(key: Any, value: Any, expireTime: Long): String ={
    val k = kryoUtil.writeClassAndObjectToByteArray(key)
    val v = kryoUtil.writeClassAndObjectToByteArray(value)
    setObject(k,v,expireTime)
  }

  /**
    *
    * @param key
    * @return
    */
  def del(key: String) = {
    jedis.del(key)
  }

  /**
    *
    * @param key
    * @return
    */
  def incr(key: String) = {
    jedis.incr(key)
  }

  /**
    *
    * @param key
    * @return
    */
  def hasKey(key: String) = {
    jedis.exists(key)
  }
}
