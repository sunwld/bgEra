package com.collie.bgEra.opdcConf

import java.util.Properties
import java.{util => ju}

import com.alibaba.druid.pool.DruidDataSource
import com.collie.bgEra.cloudApp.CloudAppContext
import com.collie.bgEra.cloudApp.dtsf.conf.DtsfConf
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.{Bean, Configuration, Import}
import org.springframework.core.io.support.PropertiesLoaderUtils
import redis.clients.jedis.{HostAndPort, JedisCluster, JedisPoolConfig}

@Configuration
@EnableAutoConfiguration
class ConfToTest {


  @Bean(Array("testzk"))
  def gettestzk(): String = {
    "133.96.6.1:2181,133.96.6.2:2181,133.96.6.3:2181"
  }

  @Bean(Array("appmContext"))
  def getCloudAppContext(): CloudAppContext = {
    val context= new CloudAppContext("test",2,3)
    context.zkUrl = "133.96.6.1:2181,133.96.6.2:2181,133.96.6.3:2181"
    context
  }



}
