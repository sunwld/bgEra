package com.collie.bgEra.cloudApp.appm.conf

import com.collie.bgEra.cloudApp.appm.{AppManagerStandardSkill, ZApplicationManager, ZookeeperDriver}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier, Value}
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}

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
@ComponentScan(Array("com.collie.bgEra.cloudApp.appm","com.collie.bgEra.cloudApp.utils"))
class AppmConf {
//  @Autowired
//  @Qualifier("appManagerStandardSkill")
//  val appManagerStandardSkill: AppManagerStandardSkill = null
//
//  @Autowired
//  val appmContext: AppmContext = null

//  @Bean(Array("zappm"))
//  def getZApplicationManager(): ZApplicationManager = {
//    val zappm = ZApplicationManager(appmContext.projectName, appmContext.minLiveServCount,
//      appmContext.clusterInitServCount, appManagerStandardSkill)
//    zappm.implementZManagement()
//    zappm
//  }

  @Bean(name = Array("zkDriver"))
  def getZkDriver(@Qualifier("zkUrl") zkUrl: String): ZookeeperDriver = {
    println("getzkDriver")
    val driver: ZookeeperDriver = new ZookeeperDriver()
    driver.connectZK(zkUrl)
    driver
  }

}
