package com.collie.bgEra.cloudApp

import java.util.Properties

import com.collie.bgEra.cloudApp.appm.ClusterInfo
import java.{util => ju}

import com.collie.bgEra.commons.util.CommonUtils
import org.springframework.core.io.{ClassPathResource, Resource}

import scala.collection.mutable
import scala.beans.BeanProperty
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

case class CloudAppContext(val projectName: String,
                      val minLiveServCount: Int, val clusterInitServCount: Int) {

  @BeanProperty
  var appmClusterInfo: ClusterInfo = null

  var quartzSchedPropMap: mutable.Map[String,Properties] = null

  init()

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

  private def init(): Unit ={
    quartzSchedPropMap = new mutable.HashMap()
    val quartzSchedPropLocations = "classpath:schedulerProp/*Scheduler.properties"
    val resolver = new PathMatchingResourcePatternResolver()
    val propRes: Array[Resource] = resolver.getResources(quartzSchedPropLocations)
    var prop: Properties = null
    propRes.foreach(propRes => {
      prop = CommonUtils.readPropertiesFile("schedulerProp/" + propRes.getFile().getName)
      quartzSchedPropMap.put(prop.getProperty("org.quartz.scheduler.instanceName"),prop)
    })
  }

}
