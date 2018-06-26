package com.collie.bgEra.eurekaSrv

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer

@SpringBootApplication
@EnableEurekaServer
class Config {

}

object Application extends App{
  SpringApplication.run(classOf[Config])
}
