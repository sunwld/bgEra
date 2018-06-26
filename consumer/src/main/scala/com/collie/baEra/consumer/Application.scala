package com.collie.baEra.consumer

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate

@SpringBootApplication
@EnableDiscoveryClient
class Config {

  @Bean
  @LoadBalanced
  def restTemplate() = {
    new RestTemplate()
  }
}

object Application extends App{
  SpringApplication.run(classOf[Config])
}
