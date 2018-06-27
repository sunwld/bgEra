package com.collie.bgEra.cloudApp

import javax.sql.DataSource
import org.mybatis.spring.annotation.MapperScan
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, EnableAspectJAutoProxy}

@Configuration
@EnableAspectJAutoProxy
@ComponentScan(Array("com.collie.bgEra.cloudApp"))
class CloudAppConf {

//  @Autowired
//  @Qualifier("mainDataSource")
//  val mainDataSource: DataSource = null


}
