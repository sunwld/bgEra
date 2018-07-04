package com.collie.bgEra.cloudApp.dsla

import com.collie.bgEra.cloudApp.appm.conf.AppmContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, EnableAspectJAutoProxy}


@Configuration
@ComponentScan(Array("com.collie.bgEra.cloudApp.dsla"))
@EnableAspectJAutoProxy
class DslaConf {
  @Autowired
  val appmContext: AppmContext = null

  @Bean(name = Array("distributedServiceLatchArbitrator"))
  def getDistributedServiceLatchArbitrator: DistributedServiceLatchArbitrator = {
    DistributedServiceLatchArbitrator(appmContext.projectName).initZookeeperForDSA()
    DistributedServiceLatchArbitrator()
  }

}
