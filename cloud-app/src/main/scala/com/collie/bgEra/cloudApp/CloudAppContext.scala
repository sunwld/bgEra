package com.collie.bgEra.cloudApp

import java.io.{File, InputStream, PrintWriter}
import java.util.{Properties, UUID}

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
import redis.clients.jedis.JedisCluster

import scala.collection.JavaConversions._
import scala.io.{BufferedSource, Source}
class CloudAppContext(val projectName: String,
                      val minLiveServCount: Int, val clusterInitServCount: Int) {

  @Autowired(required = false)
  val resourceManager: ResourceManager = null

  val quartzSchedPropMap: mutable.Map[String,Properties] = new mutable.HashMap()
  val dbSqlSessionFactoyMap: ju.Map[String,SqlSessionFactory] = new ju.HashMap()

  //context功能
  var appmClusterInfo: ClusterInfo = null
  var jedisCluster: JedisCluster = null
  var zkUrl: String = null
  private var appId: String = null

  initContext()

  //以下，实现globalRS的功能
  private var defaultDruidProp: Properties = null

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
        if (lines.hasNext()) {
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

  private def readDefaultDruidProp() = {
    defaultDruidProp = CommonUtils.readPropertiesFile("druidProps/default.properties")
  }

  private def initQuartzSchedProps() = {

    val dtsfSchedPropLocations = "classpath:schedulerProp/*Scheduler.properties"
    val extendSchedPropLocations = "classpath:extendSchedulerProp/*Scheduler.properties"
    val resolver = new PathMatchingResourcePatternResolver()
    var prop: Properties = null

    var propRes: Array[Resource] = resolver.getResources(dtsfSchedPropLocations)
    propRes.foreach(propRes => {
      prop = CommonUtils.readPropertiesFile(propRes.getFile())
      quartzSchedPropMap.put(prop.getProperty("org.quartz.scheduler.instanceName"),prop)
    })

    propRes = resolver.getResources(extendSchedPropLocations)
    propRes.foreach(propRes => {
      prop = CommonUtils.readPropertiesFile(propRes.getFile())
      quartzSchedPropMap.put(prop.getProperty("org.quartz.scheduler.instanceName"),prop)
    })
  }
}
