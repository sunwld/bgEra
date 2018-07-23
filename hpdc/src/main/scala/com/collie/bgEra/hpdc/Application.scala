package com.collie.bgEra.hpdc

import java.io.{BufferedReader, InputStream, InputStreamReader}

import com.collie.bgEra.cloudApp.dtsf.conf.DtsfConf
import com.collie.bgEra.cloudApp.utils.ContextHolder
import com.collie.bgEra.commons.util.CommonUtils
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.jdbc.{DataSourceAutoConfiguration, DataSourceTransactionManagerAutoConfiguration}
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.context.annotation._
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.web.servlet.config.annotation._
import scala.util.control.Breaks._

@EnableAutoConfiguration(exclude = Array(classOf[HibernateJpaAutoConfiguration], classOf[DataSourceTransactionManagerAutoConfiguration], classOf[DataSourceAutoConfiguration]))
@Import(Array(classOf[DtsfConf]))
@SpringBootApplication(scanBasePackages = Array("com.collie.bgEra.hpdc"), scanBasePackageClasses = Array(classOf[ContextHolder]))
class Config extends WebMvcConfigurationSupport {

  @Bean(Array("hostShellMap"))
  def getHostShellMap(): java.util.Map[String, String] = {
    val shellMap: java.util.Map[String, String] = new java.util.HashMap()
    var is: InputStream = null
    var reader: BufferedReader = null
    val script = new StringBuilder
    val resolver = new PathMatchingResourcePatternResolver()
    val shellLocations = "classpath:shells/*.xsh"
    val shellRes: Array[Resource] = resolver.getResources(shellLocations)
    shellRes.foreach(res => {
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

