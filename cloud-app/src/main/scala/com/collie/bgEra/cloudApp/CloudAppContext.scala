package com.collie.bgEra.cloudApp

import java.util.Properties

import com.collie.bgEra.cloudApp.appm.ClusterInfo
import java.{util => ju}

import com.collie.bgEra.cloudApp.dtsf.ResourceManager
import com.collie.bgEra.commons.util.CommonUtils
import org.apache.ibatis.session.SqlSessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource

import scala.collection.mutable
import scala.beans.BeanProperty
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

import scala.collection.JavaConversions._
case class CloudAppContext(val projectName: String,
                      val minLiveServCount: Int, val clusterInitServCount: Int) {

  @BeanProperty
  var appmClusterInfo: ClusterInfo = null

  @Autowired
  val resourceManager: ResourceManager = null

  val quartzSchedPropMap: mutable.Map[String,Properties] = new mutable.HashMap()
  val dbSqlSessionFactoyMap: ju.Map[String,SqlSessionFactory] = new ju.HashMap()

  private var defaultDruidProp: Properties = null
  initContext()

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

  private def initContext(): Unit ={
    val quartzSchedPropLocations = "classpath:schedulerProp/*Scheduler.properties"
    val resolver = new PathMatchingResourcePatternResolver()
    val propRes: Array[Resource] = resolver.getResources(quartzSchedPropLocations)
    var prop: Properties = null
    propRes.foreach(propRes => {
      prop = CommonUtils.readPropertiesFile("schedulerProp/" + propRes.getFile().getName)
      quartzSchedPropMap.put(prop.getProperty("org.quartz.scheduler.instanceName"),prop)
    })

    defaultDruidProp = CommonUtils.readPropertiesFile("druidProps/default.properties")
  }

  def getDefaultDruidProp(): Properties = {
    val prop = new Properties()
    defaultDruidProp.foreach(p => prop.setProperty(p._1,p._2))
    prop
  }

  def getSqlSessionFactory(name: String):SqlSessionFactory = {
    var factory = dbSqlSessionFactoyMap.get(name)
    if(factory == null){
      dbSqlSessionFactoyMap.synchronized{
        factory = dbSqlSessionFactoyMap.get(name)
        if(factory == null){
          factory = resourceManager.initDataSourceResource(name,getDefaultDruidProp())
          dbSqlSessionFactoyMap.put(name,factory)
        }
      }
    }
    factory
  }

  def putSqlSessionFactory(name: String, factory: SqlSessionFactory): Unit ={
    dbSqlSessionFactoyMap.put(name,factory)
  }


}
