package com.collie.bgEra.cloudApp.appm.conf

import com.collie.bgEra.cloudApp.CloudAppContext
import com.collie.bgEra.cloudApp.appm.{AppManagerStandardSkill, ZApplicationManager}
import com.collie.bgEra.cloudApp.base.{BaseConf, ZookeeperSession}
import com.collie.bgEra.cloudApp.dsla.DistributedServiceLatchArbitrator
import com.collie.bgEra.cloudApp.redisCache.conf.RedisCacheConf
import com.collie.bgEra.cloudApp.utils.ContextHolder
import org.springframework.beans.factory.annotation.{Autowired, Qualifier, Value}
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Import}

/**
  * APPM:cloud application manager
  * 分布式APP的必要组件，
  * 可以帮助APP实现动态的横向扩展、故障节点动态下线、再平衡任务
  * 此组件将APP的信息存放在ZOOKEEPER中，并通过ZOOKEEPER实现选举、版本控制
  * 所以，强依赖ZOOKEEPER
  * 此组件通过AppManagerStandardSkill管理APP，
  * 所以使用此组件的APP必须实现AppManagerStandardSkill，
  * 并且将AppManagerStandardSkill的实现类注册到SPRING中
  */
@Configuration
@Import(Array(classOf[BaseConf]))
@ComponentScan(basePackages = Array("com.collie.bgEra.cloudApp.appm"),basePackageClasses = Array(classOf[ContextHolder]))
class AppmConf {
  @Autowired
  @Qualifier("appManagerStandardSkill")
  val appManagerStandardSkill: AppManagerStandardSkill = null

  @Autowired
  val context: CloudAppContext = null

  @Bean(name = Array("zApplicationManager"))
  def getZApplicationManager(): ZApplicationManager = {
    ZApplicationManager(context.projectName, context.minLiveServCount,
      context.clusterInitServCount, appManagerStandardSkill).implementZManagement()
    ZApplicationManager()
  }

  @Bean(name = Array("appmZkSession"))
  def getZkDriver(): ZookeeperSession = {
    ZookeeperSession(context.zkUrl)
  }
}
