package com.collie.bgEra.cloudApp.utils

import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Component
import redis.clients.jedis.JedisCluster
import scala.collection.JavaConversions._

@Component
class RedisUtil {
  @Autowired(required = false)
  @Qualifier("jedisCluster")
  private val jedis: JedisCluster = null

  @Autowired
  private val kryoUtil: KryoUtil = null

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

  def setObject(key: Any, value: Any, expireTime: Long): String ={
    val k = kryoUtil.writeClassAndObjectToByteArray(key)
    val v = kryoUtil.writeClassAndObjectToByteArray(value)
    setObject(k,v,expireTime)
  }

  def del(key: String) = {
    jedis.del(key)
  }

  def incr(key: String) = {
    jedis.incr(key)
  }

  def hasKey(key: String) = {
    jedis.exists(key)
  }
}
