package com.collie.bgEra.cloudApp.dsla

import com.collie.bgEra.cloudApp.CloudAppContext
import com.collie.bgEra.cloudApp.base.ZookeeperSession
import com.collie.bgEra.cloudApp.utils.ContextHolder
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, EnableAspectJAutoProxy}


@Configuration
@ComponentScan(basePackages = Array("com.collie.bgEra.cloudApp.dsla"),basePackageClasses = Array(classOf[ContextHolder]))
@EnableAspectJAutoProxy
class DslaConf {
  @Autowired
  val context: CloudAppContext = null

  @Bean(name = Array("distributedServiceLatchArbitrator"))
  def getDistributedServiceLatchArbitrator: DistributedServiceLatchArbitrator = {
    DistributedServiceLatchArbitrator(context.projectName).initZookeeperForDSA()
    DistributedServiceLatchArbitrator()
  }

  @Bean(name = Array("dslaZkSession"))
  def getZkDriver(): ZookeeperSession = {
    ZookeeperSession(context.zkUrl)
  }

}
