package com.collie.bgEra.hpdc

import java.io.{BufferedReader, InputStream, InputStreamReader}
import java.util
import java.util.Properties

import com.collie.bgEra.cloudApp.dtsf.bean.{TargetInfo, TaskInfo, WorkUnitInfo, ZkSessionInfo}
import com.collie.bgEra.cloudApp.dtsf.conf.DtsfConf
import com.collie.bgEra.cloudApp.kryoUtil.KryoUtil
import com.collie.bgEra.cloudApp.utils.ContextHolder
import com.collie.bgEra.commons.util.CommonUtils
import com.collie.bgEra.hpdc.workUnit.bean.{CpuStats, HostNetStats, MemStats}
import org.apache.kafka.clients.producer.KafkaProducer
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.jdbc.{DataSourceAutoConfiguration, DataSourceTransactionManagerAutoConfiguration}
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.context.annotation._
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.web.servlet.config.annotation._
import org.apache.kafka.clients.producer.ProducerConfig
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._
import scala.util.control.Breaks._

@EnableAutoConfiguration(exclude = Array(classOf[HibernateJpaAutoConfiguration], classOf[DataSourceTransactionManagerAutoConfiguration], classOf[DataSourceAutoConfiguration]))
@Import(Array(classOf[DtsfConf]))
@SpringBootApplication(scanBasePackages = Array("com.collie.bgEra.hpdc"), scanBasePackageClasses = Array(classOf[ContextHolder]))
class Config extends WebMvcConfigurationSupport {

  private val logger: Logger = LoggerFactory.getLogger("hpdc")

  init()

  private def init(): Unit = {
    logger.info("init Application HPDC, add class to kryo.")
    val map: java.util.Map[Integer,Class[_]] = new util.HashMap()
    map.put(10001,classOf[CpuStats])
    map.put(10002,classOf[HostNetStats])
    map.put(10003,classOf[MemStats])
    KryoUtil.addCustomClassRegMap(map)
  }

  @Bean(Array("hostShellMap"))
  def getHostShellMap(): java.util.Map[String, String] = {
    val shellMap: java.util.Map[String, String] = new java.util.HashMap()
    var is: InputStream = null
    var reader: BufferedReader = null

    val resolver = new PathMatchingResourcePatternResolver()
    val shellLocations = "classpath:shells/*.xsh"
    val shellRes: Array[Resource] = resolver.getResources(shellLocations)
    shellRes.foreach(res => {
      val script = new StringBuilder
      try {
        is = this.getClass().getClassLoader().getResourceAsStream(s"shells/${res.getFilename()}")
        reader = new BufferedReader(new InputStreamReader(is))

        var line: String = null
        breakable {
          while (true) {
            line = reader.readLine()
            if (line == null) {
              break
            }
            script.append(line + "\n")
          }
        }
        shellMap.put(res.getFilename(), script.toString())
      } finally {
        if (is != null) {
          is.close()
          is = null
        }
        if (reader != null) {
          reader.close()
          reader = null
        }
      }
    })
    shellMap
  }

  @Bean(Array("hpdcProducer"))
  def getKafkaProducer(): KafkaProducer[String, Object] = {
    val producerProp = new Properties()
    producerProp.setProperty(ProducerConfig.ACKS_CONFIG,"1")
    producerProp.setProperty(ProducerConfig.RETRIES_CONFIG,"1")
    producerProp.setProperty(ProducerConfig.BATCH_SIZE_CONFIG,"16384")
    producerProp.setProperty(ProducerConfig.LINGER_MS_CONFIG,"1000")
    producerProp.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG,"snappy")
    producerProp.setProperty(ProducerConfig.BUFFER_MEMORY_CONFIG,"100663296")
    producerProp.setProperty(ProducerConfig.MAX_REQUEST_SIZE_CONFIG,"1073741824")
    producerProp.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"com.collie.bgEra.hpdc.kafka.serializers.ObjectSerializer")
    producerProp.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"com.collie.bgEra.hpdc.kafka.serializers.ObjectSerializer")
    val customProp = CommonUtils.readPropertiesFile("hpdcKafkaProducer.properties")

    customProp.foreach(p => producerProp.setProperty(p._1,p._2))
    new KafkaProducer[String,Object](producerProp)
  }
}

object Application extends App {
  SpringApplication.run(classOf[Config])
}

