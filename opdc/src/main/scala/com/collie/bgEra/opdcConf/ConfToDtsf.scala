package com.collie.bgEra.opdcConf

import java.util
import java.util.Properties

import com.alibaba.druid.pool.DruidDataSource
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

  @Bean(Array("jedisCluster"))
  def jedisCluster: JedisCluster = {
    val jedisPoolConfig = new JedisPoolConfig()
    // 最大空闲连接数, 默认8个// 最大空闲连接数, 默认8个
    jedisPoolConfig.setMaxIdle(20)
    // 最大连接数, 默认8个
    jedisPoolConfig.setMaxTotal(100)
    //最小空闲连接数, 默认0
    jedisPoolConfig.setMinIdle(10)
    // 获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
    jedisPoolConfig.setMaxWaitMillis(2000) // 设置2秒
    jedisPoolConfig.setBlockWhenExhausted(true)
    jedisPoolConfig.setTestWhileIdle(true)
    jedisPoolConfig.setMinEvictableIdleTimeMillis(90000)
    jedisPoolConfig.setTimeBetweenEvictionRunsMillis(10000)
    jedisPoolConfig.setNumTestsPerEvictionRun(-1)
    //对拿到的connection进行validateObject校验
    jedisPoolConfig.setTestOnBorrow(true)

    val set = new util.HashSet[HostAndPort]()
    set.add(new HostAndPort("133.96.6.1", 10001))
    set.add(new HostAndPort("133.96.6.2", 10001))
    set.add(new HostAndPort("133.96.6.3", 10001))
    set.add(new HostAndPort("133.96.6.4", 10001))
    set.add(new HostAndPort("133.96.6.5", 10001))
    set.add(new HostAndPort("133.96.6.6", 10001))

    val cluster = new JedisCluster(set, 3000, 10000, 2, "redis%123", jedisPoolConfig)
    cluster
  }
}
