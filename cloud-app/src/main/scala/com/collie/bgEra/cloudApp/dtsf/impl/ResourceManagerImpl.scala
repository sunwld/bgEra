package com.collie.bgEra.cloudApp.dtsf.impl

import java.util.Properties

import com.collie.bgEra.cloudApp.dtsf.{ResourceManager, TaskManager}
import com.collie.bgEra.cloudApp.dtsf.bean.{JmxConnPoolResource, ResourceType}
import org.springframework.stereotype.Component
import com.alibaba.druid.util.Utils.getBoolean

import scala.collection.JavaConversions._
import java.{util => ju}

import com.alibaba.druid.pool.DruidDataSource
import com.collie.bgEra.cloudApp.context.CloudAppContext
import com.collie.bgEra.cloudApp.dtsf.mapper.TaskMapper
import org.apache.ibatis.session.SqlSessionFactory
import org.mybatis.spring.SqlSessionFactoryBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import com.collie.bgEra.cloudApp.ssh2Pool.{Ssh2Session, Ssh2SessionPool, SshConnFactory}
import org.apache.commons.pool2.impl.GenericObjectPool
import org.apache.commons.pool2.impl.GenericObjectPoolConfig

@Component
class ResourceManagerImpl extends ResourceManager{
  @Autowired
  @Lazy
  private val taskMapper: TaskMapper = null
  @Autowired
  private val cloudAppContext: CloudAppContext = null

  private val dbSqlSessionFactoyMap: java.util.Map[String,SqlSessionFactory] = new java.util.HashMap()


  override def getDataSourceResource(targetId: String): SqlSessionFactory = {
    var factory = dbSqlSessionFactoyMap.get(targetId)
    if(factory == null){
      dbSqlSessionFactoyMap.synchronized{
        factory = dbSqlSessionFactoyMap.get(targetId)
        if(factory == null){
          factory = initDataSourceResource(targetId)
          dbSqlSessionFactoyMap.put(targetId,factory)
        }
      }
    }
    factory
  }

  override def initDataSourceResource(targetId: String): SqlSessionFactory = {
    val defaultProp = cloudAppContext.getDefaultDruidProp()
    val dataSource = new DruidDataSource()
    val sqlSessionFactoryBean = new SqlSessionFactoryBean()
    val prop = taskMapper.qryResourceParamsById(targetId,ResourceType.DBDATASOURCE)

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


  private val ssh2ConnPoolsMap: java.util.Map[String,Ssh2SessionPool] = new ju.HashMap()
  override def getHostSshConnPoolResource(targetId: String): Ssh2Session = {
    var pool = ssh2ConnPoolsMap.get(targetId)
    if(pool == null){

      ssh2ConnPoolsMap.synchronized{
        pool = ssh2ConnPoolsMap.get(targetId)
        if(pool == null){
          pool = initHostSshConnPoolResource(targetId)
          ssh2ConnPoolsMap.put(targetId,pool)
        }
      }
    }
    pool.borrowObject()
  }

  override def initHostSshConnPoolResource(targetId: String): Ssh2SessionPool = {
    val poolConfig = new GenericObjectPoolConfig()
    poolConfig.setTestWhileIdle(true) //空闲时进行连接测试，会启动异步evict线程进行失效检测
    poolConfig.setMinEvictableIdleTimeMillis(1800000) //连接的空闲的最长时间，需要testWhileIdle为true
    poolConfig.setTimeBetweenEvictionRunsMillis(30000) // 失效检测时间，需要testWhileIdle为true，默认5分钟
    poolConfig.setNumTestsPerEvictionRun(3) // 每次检查连接的数量，需要testWhileIdle为true
    poolConfig.setTestOnBorrow(true) // 获取连接时检测连接的有效性
    poolConfig.setTestOnReturn(false) // 返还连接时检测连接的有效性
    poolConfig.setMaxTotal(10) // 总连接数
    poolConfig.setMinIdle(2) //最小空闲
    poolConfig.setMaxIdle(3) //最大空闲
    poolConfig.setFairness(true) // 多个任务需要borrow连接时，阻塞时是否采用公平策略，为true时采用，按照先申请先获得的策略进行borrow操作
    poolConfig.setBlockWhenExhausted(true) // 设置为true时，池中无可用连接，borrow时进行阻塞；为false时，当池中无可用连接，抛出NoSuchElementException异常
    poolConfig.setMaxWaitMillis(5000) // 最大等待时间，当需要borrow一个连接时，最大的等待时间，如果超出时间，抛出NoSuchElementException异常，-1为不限制时间

    val prop = taskMapper.qryResourceParamsById(targetId,ResourceType.HOSTSSHTPOOL)
    val factory = new SshConnFactory(prop.getProperty("hostIp"), prop.getProperty("hostPort").toInt, prop.getProperty("userName"), prop.getProperty("password"))
    val pool: Ssh2SessionPool = new Ssh2SessionPool(factory, poolConfig)
    pool
  }

  override def getJmxConnPoolResource(targetId: String): JmxConnPoolResource = ???

  override def initJmxConnPoolResource(resourceContext: Properties): Unit = ???
}
