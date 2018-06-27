package com.collie.bgEra.cloudApp.dtsf.conf

import com.alibaba.druid.pool.DruidDataSource
import com.collie.bgEra.cloudApp.appm.{AppManagerStandardSkill, ZApplicationManager}
import com.collie.bgEra.cloudApp.appm.conf.{AppmConf, AppmContext}
import com.collie.bgEra.cloudApp.redisCache.conf.RedisCacheConf
import org.mybatis.spring.SqlSessionFactoryBean
import org.mybatis.spring.annotation.MapperScan
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Import}
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

@Configuration
@Import(Array(classOf[AppmConf],classOf[RedisCacheConf]))
@ComponentScan(Array("com.collie.bgEra.cloudApp.dtsf"))
@MapperScan(basePackages = Array("com.collie.bgEra.cloudApp.dtsf.mapper"))
class DtsfConf {

  @Autowired
  @Qualifier("dtfsDataSource")
  val dtfsDataSource: DruidDataSource = null

  @Autowired
  @Qualifier("appManagerStandardSkill")
  val appManagerStandardSkill: AppManagerStandardSkill = null

  @Autowired
  val appmContext: AppmContext = null

  @Bean
  def sqlSeesionFatory(): SqlSessionFactoryBean = {
    val sqlSessionFactoryBean = new SqlSessionFactoryBean
    sqlSessionFactoryBean.setDataSource(dtfsDataSource)
    var resolver = new PathMatchingResourcePatternResolver()
    sqlSessionFactoryBean.setMapperLocations(resolver.getResources("classpath:com/collie/bgEra/cloudApp/dtsf/mapper/*Mapper.xml"))
    sqlSessionFactoryBean
  }

  @Bean(Array("zappm"))
  def getZApplicationManager(): ZApplicationManager = {
    val zappm = ZApplicationManager(appmContext.projectName, appmContext.zkUrls, appmContext.minLiveServCount,
      appmContext.clusterInitServCount, appManagerStandardSkill)
    zappm.implementZManagement()
    zappm
  }
}
