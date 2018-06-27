package com.collie.bgEra.cloudApp

import org.mybatis.spring.annotation.MapperScan
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, EnableAspectJAutoProxy}

@Configuration
@EnableAspectJAutoProxy
@ComponentScan(Array("com.collie.bgEra.cloudApp"))
class CloudAppConf {

}
