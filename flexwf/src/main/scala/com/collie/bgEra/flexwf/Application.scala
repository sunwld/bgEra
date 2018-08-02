package com.collie.bgEra.flexwf

import com.collie.bgEra.cloudApp.redisCache.conf.RedisCacheConf
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.context.annotation.{EnableAspectJAutoProxy, Import}

@Import(Array(classOf[RedisCacheConf]))
@SpringBootApplication
@EnableAspectJAutoProxy
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
class Configuration {

}

object Application extends App {
  SpringApplication.run(classOf[Configuration])
}
