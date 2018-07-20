package com.collie.bgEra.cloudApp.context

import java.util.Properties

import com.collie.bgEra.commons.util.CommonUtils
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}
import scala.collection.JavaConversions._


@Configuration
@ComponentScan(basePackageClasses = Array(classOf[CloudAppContext]))
class ContextConf {

  @Bean(Array("cloudAppProps"))
  def loadCloudAppProps(): Properties = {
    val defaultProp: Properties = CommonUtils.readPropertiesFile("cloudAppProps/cloudappDefault.properties")
    val customProps: Properties = CommonUtils.readPropertiesFile("cloudapp.properties")
    defaultProp.foreach(p => {
      customProps.setProperty(p._1,p._2)
    })
    customProps
  }
}
