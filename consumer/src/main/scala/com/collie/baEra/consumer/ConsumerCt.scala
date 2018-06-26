package com.collie.baEra.consumer

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{RequestMapping, RestController}
import org.springframework.web.client.RestTemplate

@RestController
@RequestMapping(Array("/consumer"))
class ConsumerCt {

  @Autowired
  var restTemplate : RestTemplate = _

  @RequestMapping(Array("/users"))
  def consumerUsers = {
    println("ConsumerCt")
    restTemplate.getForObject("http://service-hi/opdc/users",classOf[String])
  }
}
