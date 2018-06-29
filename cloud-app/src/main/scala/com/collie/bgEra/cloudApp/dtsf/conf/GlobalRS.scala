package com.collie.bgEra.cloudApp.dtsf.conf

import com.collie.bgEra.commons.util.ContextHolder
import redis.clients.jedis.JedisCluster

object GlobalRS {

  def getJedisCluster() ={
    ContextHolder.getBeanSafe(classOf[JedisCluster])
  }

}
