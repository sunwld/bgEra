package com.collie.bgEra.hpdc

import java.io.{BufferedReader, InputStream, InputStreamReader}
import java.util

import com.collie.bgEra.cloudApp.dtsf.conf.DtsfConf
import com.collie.bgEra.cloudApp.kryoUtil.KryoUtil
import com.collie.bgEra.cloudApp.redisCache.RedisCacheAspect
import com.collie.bgEra.cloudApp.redisCache.conf.RedisCacheConf
import com.collie.bgEra.cloudApp.utils.ContextHolder
import com.collie.bgEra.hpdc.service.bean.CalculateIncacheStatsValue
import com.collie.bgEra.hpdc.workUnit.bean._
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.context.annotation._
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.web.servlet.config.annotation._
import org.slf4j.{Logger, LoggerFactory}

import scala.util.control.Breaks._

@Import(Array(classOf[DtsfConf], classOf[RedisCacheConf]))
@EnableAspectJAutoProxy
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
@SpringBootApplication(scanBasePackages = Array("com.collie.bgEra.hpdc"), scanBasePackageClasses = Array(classOf[ContextHolder]))
class Config extends WebMvcConfigurationSupport {

  private val logger: Logger = LoggerFactory.getLogger("hpdc")

  init()

  private def init(): Unit = {
    logger.info("init Application HPDC, add class to kryo.")
    val map: java.util.Map[Integer, Class[_]] = new util.HashMap()
    map.put(10001, classOf[CpuStats])
    map.put(10002, classOf[HostNetStats])
    map.put(10003, classOf[MemStats])
    map.put(10004, classOf[CpuProcessorStats])
    map.put(10005, classOf[FilesystemUsageStats])
    map.put(10006, classOf[HostIOStats])
    map.put(10007, classOf[HostNetStats])
    map.put(10008, classOf[LinuxHugePagesUsageStats])
    map.put(10009, classOf[NetworkErrorsStats])
    map.put(10010, classOf[SharedMemorySegStats])
    map.put(10011, classOf[SwapIOStats])
    map.put(10012, classOf[CalculateIncacheStatsValue[_]])
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
}

object Application extends App {
  SpringApplication.run(classOf[Config])
}

