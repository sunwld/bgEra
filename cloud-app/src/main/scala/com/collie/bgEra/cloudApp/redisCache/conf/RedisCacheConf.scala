package com.collie.bgEra.cloudApp.redisCache.conf

import com.alibaba.druid.pool.DruidDataSource
import com.collie.bgEra.cloudApp.appm.ZApplicationManager
import org.mybatis.spring.SqlSessionFactoryBean
import org.mybatis.spring.annotation.MapperScan
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

@Configuration
@ComponentScan(Array("com.collie.bgEra.cloudApp.redisCache"))
class RedisCacheConf {


}
