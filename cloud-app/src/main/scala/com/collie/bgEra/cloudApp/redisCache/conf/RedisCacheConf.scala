package com.collie.bgEra.cloudApp.redisCache.conf

import java.util.Properties

import com.collie.bgEra.cloudApp.dsla.DslaConf
import com.collie.bgEra.cloudApp.utils.ContextHolder
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.context.annotation._
import org.springframework.util.Assert
import redis.clients.jedis.{HostAndPort, JedisCluster, JedisPoolConfig}


/**
  * redisCache功能模块：
  *
  * @author yjd swl
  * @todo 动态缓存数据：
  *       1.缓存从MYSQL加载的数据，支持过期时间，支持清除缓存
  *       2.缓存列表，维持一个最新数据列表
  *       3.缓存ZSET，维持一个用户关心的排行榜
  * @note 依赖：
  *       Autowired
  *       Qualifier("jedisCluster")
  *       private val jedis: JedisCluster = null
  */
@Configuration
@ComponentScan(basePackages = Array("com.collie.bgEra.cloudApp.redisCache"),basePackageClasses = Array(classOf[ContextHolder]))
@Import(Array(classOf[DslaConf]))
@EnableAspectJAutoProxy
class RedisCacheConf {

  @Autowired
  @Qualifier("cloudAppProps")
  private val props: Properties = null

  @Bean(Array("cloudAppJedisCluster"))
  def getJedisCluster(): JedisCluster = {
    val redisUrl = props.getProperty("reidsCache.redisClusterUrl")
    Assert.hasText(redisUrl,"reidsCache.redisClusterUrl must not be empty")
    val password = props.getProperty("reidsCache.redisClusterPassword")
    Assert.hasText(password,"reidsCache.redisClusterPassword must not be empty")

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
    val set = new java.util.HashSet[HostAndPort]()
    redisUrl.split(",").foreach(u => {
      val split: Array[String] = u.split(":")
      set.add(new HostAndPort(split(0), split(1).toInt))
    })
    val cluster = new JedisCluster(set, 3000, 10000, 2, password, jedisPoolConfig)
    cluster
  }

}
