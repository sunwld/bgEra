package com.collie.bgEra.cloudApp.context

import java.io.{File, PrintWriter}
import java.util.{Properties, UUID}

import com.collie.bgEra.commons.util.CommonUtils
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.core.io.{ClassPathResource, Resource}
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._

import scala.collection.mutable
import scala.io.{BufferedSource, Source}

@Component
class CloudAppContext {
  private val logger: Logger = LoggerFactory.getLogger(classOf[CloudAppContext])
  private var appId: String = null
  val quartzSchedPropMap: mutable.Map[String,Properties] = new mutable.HashMap()
  private var defaultDruidProp: Properties = null

  initContext()

  def getAppId(): String={
    appId
  }

  private def initContext(): Unit ={
    initQuartzSchedProps()
    readDefaultDruidProp()
    genAppId()
  }

  private def genAppId()={
    val filePath = System.getProperty("user.dir") + "/myid"
    val file = new File(filePath)
    var source: BufferedSource = null
    var writer: PrintWriter = null
    try {
      if (file.exists()) {
        val lines = Source.fromFile(file).getLines()
        if (lines.hasNext) {
          this.appId = lines.next()
        }
      }

      if(this.appId == null){
        this.appId = UUID.randomUUID().toString()
        writer = new PrintWriter(file)
        writer.println(this.appId)
        writer.flush()
      }
    } finally{
      if(source != null){
        source.close()
      }
      if(writer != null){
        writer.close()
      }
    }
  }

  def getQuartzSchedulerPorp(poolName: String) ={
    var name = getSchedulerNameByThreadPool(poolName)
    val resultProp = quartzSchedPropMap.get(name) match {
      case None => {
        val tempProp: Properties = quartzSchedPropMap("templateScheduler")
        tempProp.setProperty("org.quartz.scheduler.instanceName",name)
        new Properties(tempProp)
      }
      case Some(value) => value
    }
    resultProp
  }

  def getSchedulerNameByThreadPool(poolName: String): String ={
    s"${poolName}Scheduler"
  }

  private def initQuartzSchedProps() = {
    logger.info("cache quartz scheduler properties ...")
    val dtsfSchedPropLocations = "classpath:cloudAppProps/schedulerProp/*Scheduler.properties"
    val extendSchedPropLocations = "classpath:extendSchedulerProp/*Scheduler.properties"
    val resolver = new PathMatchingResourcePatternResolver()
    var prop: Properties = null

    var propRes: Array[Resource] = resolver.getResources(dtsfSchedPropLocations)
    propRes.foreach(propRes => {
      prop = CommonUtils.readPropertiesFile("cloudAppProps/schedulerProp/" + propRes.getFilename())
      quartzSchedPropMap.put(prop.getProperty("org.quartz.scheduler.instanceName"),prop)
    })

    if (new ClassPathResource("extendSchedulerProp").exists()) {
      propRes = resolver.getResources(extendSchedPropLocations)
      propRes.foreach(propRes => {
        prop = CommonUtils.readPropertiesFile("extendSchedulerProp/" + propRes.getFilename())
        quartzSchedPropMap.put(prop.getProperty("org.quartz.scheduler.instanceName"),prop)
      })
    }

    logger.info(s"cache quartz scheduler properties end. propertes: ")
    quartzSchedPropMap.foreach(m => logger.info(m+""))
  }

  private def readDefaultDruidProp() = {
    defaultDruidProp = CommonUtils.readPropertiesFile("cloudAppProps/druidProps/default.properties")
  }

  def getDefaultDruidProp(): Properties = {
    val prop = new Properties()
    defaultDruidProp.foreach(p => prop.setProperty(p._1,p._2))
    prop
  }
}
