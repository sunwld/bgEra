package com.collie.bgEra.cloudApp.dsla

import java.util.Properties

import com.collie.bgEra.cloudApp.base.ZookeeperSession
import com.collie.bgEra.cloudApp.context.{CloudAppContext, ContextConf}
import com.collie.bgEra.cloudApp.utils.ContextHolder
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.context.annotation._
import org.springframework.util.Assert


@Configuration
@ComponentScan(basePackages = Array("com.collie.bgEra.cloudApp.dsla"), basePackageClasses = Array(classOf[ContextHolder]))
@Import(Array(classOf[ContextConf]))
@EnableAspectJAutoProxy
class DslaConf {
  @Autowired
  @Qualifier("cloudAppProps")
  private val props: Properties = null

  @Bean(name = Array("dslaZkSession"))
  def getZkDriver(): ZookeeperSession = {
    val zkUrl = props.getProperty("dsla.zkUrl")
    Assert.hasText(zkUrl, "dsla.zkUrl must not be empty")
    ZookeeperSession(zkUrl)
  }

  @Bean(name = Array("distributedServiceLatchArbitrator"))
  def getDistributedServiceLatchArbitrator(@Qualifier("dslaZkSession") zkSession: ZookeeperSession): DistributedServiceLatchArbitrator = {
    val projectName = props.getProperty("projectName")
    Assert.hasText(projectName, "projectName must not be empty")
    DistributedServiceLatchArbitrator(projectName).initZookeeperForDSA(zkSession)
    DistributedServiceLatchArbitrator()
  }

}
