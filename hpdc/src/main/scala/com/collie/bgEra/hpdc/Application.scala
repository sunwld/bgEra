package com.collie.bgEra.hpdc

import com.collie.bgEra.cloudApp.dtsf.conf.DtsfConf
import com.collie.bgEra.cloudApp.utils.ContextHolder
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.jdbc.{DataSourceAutoConfiguration, DataSourceTransactionManagerAutoConfiguration}
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.context.annotation._
import org.springframework.web.servlet.config.annotation._

@EnableAutoConfiguration(exclude=Array(classOf[HibernateJpaAutoConfiguration],classOf[DataSourceTransactionManagerAutoConfiguration],classOf[DataSourceAutoConfiguration]))
@Import(Array(classOf[DtsfConf]))
@SpringBootApplication(scanBasePackages = Array("com.collie.bgEra.hpdc"),scanBasePackageClasses = Array(classOf[ContextHolder]))
class Config extends WebMvcConfigurationSupport{
}

object Application extends App{
    SpringApplication.run(classOf[Config])
}

