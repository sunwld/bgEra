package com.collie.bgEra.opdcConf

import java.util.Properties
import java.{util => ju}

import com.collie.bgEra.cloudApp.dtsf.conf.DtsfConf
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.{Bean, Configuration, Import}
import redis.clients.jedis.{HostAndPort, JedisCluster, JedisPoolConfig}

@Configuration
@Import(Array(classOf[DtsfConf]))
class ConfToDtsf {

}
