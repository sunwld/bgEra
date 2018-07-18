package com.collie.bgEra.opdc

import java.util.Properties
import java.{util => ju}

import com.alibaba.druid.pool.DruidDataSource
import com.collie.bgEra.cloudApp.CloudAppContext
import com.collie.bgEra.cloudApp.dtsf.conf.DtsfConf
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.{Bean, Configuration, Import}
import redis.clients.jedis.{HostAndPort, JedisCluster, JedisPoolConfig}

@Configuration
@EnableAutoConfiguration
@Import(Array(classOf[DtsfConf]))
class ConfToDtsf {
  @Bean(name = Array("dtfsDataSource"),destroyMethod = "close")
  def dataSource: DruidDataSource = {
    val connPros = new Properties()
    connPros.setProperty("druid.driverClassName","com.mysql.jdbc.Driver")
    connPros.setProperty("druid.username","dtsf")
    connPros.setProperty("druid.password","1234yjd")
    connPros.setProperty("druid.url","jdbc:mysql://133.96.6.1:3306/dtsfdb?characterEncoding=utf-8&useSSL=false")

    connPros.setProperty("druid.initialSize","30")
    connPros.setProperty("druid.minIdle","30")
    connPros.setProperty("druid.maxActive","100")
    connPros.setProperty("druid.poolPreparedStatements","false")
    connPros.setProperty("druid.maxPoolPreparedStatementPerConnectionSize","0")
    connPros.setProperty("druid.validationQuery","SELECT 1 FROM dual")
    connPros.setProperty("druid.testOnBorrow","true")
    connPros.setProperty("druid.testWhileIdle","true")
    connPros.setProperty("druid.timeBetweenEvictionRunsMillis","30000")
    connPros.setProperty("druid.minEvictableIdleTimeMillis","900000")

    connPros.setProperty("druid.filters","stat,wall")

    val dataSource = new DruidDataSource()
    dataSource.setDefaultAutoCommit(false)
    dataSource.setMaxWait(30000)
    dataSource.setValidationQueryTimeout(3)
    dataSource.setRemoveAbandoned(true)
    dataSource.setRemoveAbandonedTimeout(180)
    dataSource.setLogAbandoned(true)
    val slf4jLogFilter = new com.alibaba.druid.filter.logging.Slf4jLogFilter()
    slf4jLogFilter.setStatementExecutableSqlLogEnable(false)
    dataSource.setProxyFilters(ju.Arrays.asList(slf4jLogFilter))
    dataSource.configFromPropety(connPros)
    dataSource
  }

  @Bean(Array("appmContext"))
  def getCloudAppContext(): CloudAppContext = {
    val context= new CloudAppContext("test",2,3)
    context.jedisCluster = getJedisCluster()
    context.zkUrl = "133.96.6.1:2181,133.96.6.2:2181,133.96.6.3:2181"
    context
  }


  private def getJedisCluster(): JedisCluster = {
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
    val set = new ju.HashSet[HostAndPort]()
    //    set.add(new HostAndPort("192.168.186.100", 10001))
    //    set.add(new HostAndPort("192.168.186.100", 10003))
    //    set.add(new HostAndPort("192.168.186.101", 10001))
    //    set.add(new HostAndPort("192.168.186.101", 10003))
    //    set.add(new HostAndPort("192.168.186.102", 10001))
    //    set.add(new HostAndPort("192.168.186.102", 10003))
    set.add(new HostAndPort("133.96.6.1", 10001))
    set.add(new HostAndPort("133.96.6.2", 10001))
    set.add(new HostAndPort("133.96.6.3", 10001))
    set.add(new HostAndPort("133.96.6.4", 10001))
    set.add(new HostAndPort("133.96.6.5", 10001))
    set.add(new HostAndPort("133.96.6.6", 10001))
    val cluster = new JedisCluster(set, 3000, 10000, 2, "redis%123"/*"123456"*/, jedisPoolConfig)
    cluster
  }
}
