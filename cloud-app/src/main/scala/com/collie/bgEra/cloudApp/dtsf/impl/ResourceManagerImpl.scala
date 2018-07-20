package com.collie.bgEra.cloudApp.dtsf.impl

import java.util.Properties

import com.collie.bgEra.cloudApp.dtsf.{ResourceManager, TaskManager}
import com.collie.bgEra.cloudApp.dtsf.bean.{HostSshConnPoolResource, JmxConnPoolResource}
import org.springframework.stereotype.Component
import com.alibaba.druid.util.Utils.getBoolean

import scala.collection.JavaConversions._
import java.{util => ju}

import com.alibaba.druid.pool.DruidDataSource
import com.collie.bgEra.cloudApp.context.CloudAppContext
import com.collie.bgEra.cloudApp.dtsf.mapper.TaskMapper
import com.collie.bgEra.cloudApp.utils.ContextHolder
import org.apache.ibatis.session.SqlSessionFactory
import org.mybatis.spring.SqlSessionFactoryBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy

@Component
class ResourceManagerImpl extends ResourceManager{

  @Autowired
  @Lazy
  private val taskMapper: TaskMapper = null

  @Autowired
  private val cloudAppContext: CloudAppContext = null

  private val dbSqlSessionFactoyMap: java.util.Map[String,SqlSessionFactory] = new java.util.HashMap()
  private var defaultDruidProp: Properties = null

  override def getDataSourceResource(resourceName: String): SqlSessionFactory = {
    var factory = dbSqlSessionFactoyMap.get(resourceName)
    if(factory == null){
      dbSqlSessionFactoyMap.synchronized{
        factory = dbSqlSessionFactoyMap.get(resourceName)
        if(factory == null){
          factory = initDataSourceResource(resourceName,cloudAppContext.getDefaultDruidProp())
          dbSqlSessionFactoyMap.put(resourceName,factory)
        }
      }
    }
    factory
  }

  override def initDataSourceResource(resourceName: String,defaultProp: Properties): SqlSessionFactory = {
    val dataSource = new DruidDataSource()
    val sqlSessionFactoryBean = new SqlSessionFactoryBean()
    val prop = taskMapper.qryResourcePropByName(resourceName)

    defaultProp.foreach(p => prop.setProperty(p._1,p._2))
    dataSource.configFromPropety(prop)

    dataSource.setDefaultAutoCommit(getBoolean(prop,"druid.defaultAutoCommit"))
    dataSource.setMaxWait(prop.getProperty("druid.maxWait","10000").toInt)
    dataSource.setValidationQueryTimeout(prop.getProperty("druid.validationQueryTimeout","3").toInt)
    dataSource.setRemoveAbandoned(getBoolean(prop,"druid.removeAbandoned"))
    dataSource.setRemoveAbandonedTimeout(prop.getProperty("druid.removeAbandonedTimeout","180").toInt)
    dataSource.setLogAbandoned(getBoolean(prop,"druid.logAbandoned"))
    val slf4jLogFilter = new com.alibaba.druid.filter.logging.Slf4jLogFilter()
    slf4jLogFilter.setStatementExecutableSqlLogEnable(false)
    dataSource.setProxyFilters(ju.Arrays.asList(slf4jLogFilter))

    sqlSessionFactoryBean.setDataSource(dataSource)
    sqlSessionFactoryBean.getObject()
  }

  def putSqlSessionFactoy(name: String,factory: SqlSessionFactory) = {
    dbSqlSessionFactoyMap.put(name,factory)
  }

  override def getHostSshConnPoolResource(resourceName: String): HostSshConnPoolResource = ???

  override def initHostSshConnPoolResource(resourceContext: Properties): Unit = ???

  override def getJmxConnPoolResource(resourceName: String): JmxConnPoolResource = ???

  override def initJmxConnPoolResource(resourceContext: Properties): Unit = ???
}
