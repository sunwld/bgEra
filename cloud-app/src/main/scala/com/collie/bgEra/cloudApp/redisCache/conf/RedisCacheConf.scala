package com.collie.bgEra.cloudApp.redisCache.conf

import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}


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
@ComponentScan(Array("com.collie.bgEra.cloudApp.redisCache"))
class RedisCacheConf {


}