package com.collie.bgEra.opdcConf

import java.util.Properties

import com.alibaba.druid.pool.DruidDataSource
import com.collie.bgEra.cloudApp.appm.conf.AppmContext
import com.collie.bgEra.cloudApp.dtsf.conf.DtsfConf
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.{Bean, Configuration, Import}
import redis.clients.jedis.{HostAndPort, JedisCluster, JedisPoolConfig}

@Configuration
@EnableAutoConfiguration
@Import(Array(classOf[DtsfConf]))
class ConfToDtsf {
  @Bean(Array("dtfsDataSource"))
  def dataSource: DruidDataSource = {
    val connPros = new Properties()
    connPros.setProperty("druid.driverClassName","oracle.jdbc.OracleDriver")
    connPros.setProperty("druid.username","scifmation")
    connPros.setProperty("druid.password","kxht#123")
    connPros.setProperty("druid.url","jdbc:oracle:thin:@133.96.9.118:7521:orcl")
    connPros.setProperty("druid.name","testDS")
    val dataSource = new DruidDataSource()
    dataSource.configFromPropety(connPros)
    dataSource
  }

  @Bean(Array("appmContext"))
  def getAppmContext = AppmContext("test","133.96.6.1:2181,133.96.6.2:2181,133.96.6.3:2181",2,3)

  @Bean(Array("jedisCluster"))
  def jedisCluster: JedisCluster = {
    val jedisPoolConfig = new JedisPoolConfig()
    new JedisCluster(new HostAndPort("133.96.6.1", 10001), 2, 5, 2, "redis%123", jedisPoolConfig)
  }
}
