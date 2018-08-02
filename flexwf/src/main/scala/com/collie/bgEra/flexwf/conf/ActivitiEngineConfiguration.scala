package com.collie.bgEra.flexwf.conf

import java.util.Properties

import com.alibaba.druid.pool.DruidDataSource
import com.collie.bgEra.cloudApp.context.CloudAppContext
import org.activiti.engine._
import org.activiti.spring.SpringProcessEngineConfiguration
import org.apache.ibatis.session.SqlSessionFactory
import org.mybatis.spring.SqlSessionFactoryBean
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.jdbc.datasource.DataSourceTransactionManager

import scala.collection.JavaConversions._

@Configuration
class ActivitiEngineConfiguration {

  @Autowired
  @Qualifier("cloudAppProps")
  private val props: Properties = null
  @Autowired
  private val cloudAppContext: CloudAppContext = null

  @Bean(Array("processEngineConfiguration"))
  def initializeProcessEngineConfiguration(@Qualifier("flexwfdatasource") dataSource: DruidDataSource,
                                           @Qualifier("transactionManager") transactionManager: DataSourceTransactionManager): SpringProcessEngineConfiguration = {

    val processEngineConfiguration = new SpringProcessEngineConfiguration()
    processEngineConfiguration.setTransactionManager(transactionManager)

    processEngineConfiguration.setDatabaseSchemaUpdate("true")
    processEngineConfiguration.setJobExecutorActivate(true)
    processEngineConfiguration.setAsyncExecutorEnabled(true)
    processEngineConfiguration.setAsyncExecutorActivate(true)
    processEngineConfiguration.setDataSource(dataSource)

    processEngineConfiguration
  }

  @Bean(Array("transactionManager"))
  def initializeTransactionManager(@Qualifier("flexwfdatasource") dataSource: DruidDataSource): DataSourceTransactionManager = {
    new DataSourceTransactionManager(dataSource)
  }

  @Bean(Array("flexwfdatasource"))
  @ConfigurationProperties(prefix="spring.datasource")
  def initializeFlexwfdatasource(): DruidDataSource = {
    val druidPros = new Properties()
    cloudAppContext.getDefaultDruidProp().foreach(p => {
      druidPros.setProperty(p._1, p._2)
    })

    val dataSource = new DruidDataSource()
    dataSource.setDefaultAutoCommit(false)
    dataSource.setRemoveAbandoned(true)
    dataSource.setRemoveAbandonedTimeout(180)
    dataSource.setLogAbandoned(true)
    val slf4jLogFilter = new com.alibaba.druid.filter.logging.Slf4jLogFilter()
    slf4jLogFilter.setStatementExecutableSqlLogEnable(false)

    dataSource.setProxyFilters(java.util.Arrays.asList(slf4jLogFilter))
    dataSource.configFromPropety(druidPros)

    dataSource
  }

  @Bean(Array("flexwfSqlsessionFactory"))
  def initializeFlexwfSqlsessionFactory(@Qualifier("flexwfdatasource") datasource: DruidDataSource): SqlSessionFactory = {

    val mybatisConfXmlPath: String = props.getProperty("flexwf.mybatisConf")

    val factoryBean = new SqlSessionFactoryBean()
    val resolver = new PathMatchingResourcePatternResolver()
    val factoryCfgResource = resolver.getResource(mybatisConfXmlPath)
    factoryBean.setConfigLocation(factoryCfgResource)
    factoryBean.setDataSource(datasource)

    factoryBean.getObject()
  }


  @Bean(Array("processEngine"))
  def initializeActivityEngine(@Qualifier("processEngineConfiguration")
                               processEngineConfiguration: SpringProcessEngineConfiguration): ProcessEngine = {

    processEngineConfiguration.buildProcessEngine()
  }

  @Bean
  def getWorkFlowTaskService(@Qualifier("processEngine") engine: ProcessEngine): TaskService = {

    engine.getTaskService()
  }

  @Bean
  def getWorkFlowRepositoryService(@Qualifier("processEngine") engine: ProcessEngine): RepositoryService = {

    engine.getRepositoryService()
  }

  @Bean
  def getWorkFlowHistoryService(@Qualifier("processEngine") engine: ProcessEngine): HistoryService = {

    engine.getHistoryService()
  }


  @Bean
  def getWorkFlowManagementService(@Qualifier("processEngine") engine: ProcessEngine): ManagementService = {

    engine.getManagementService()
  }

  @Bean
  def getWorkFlowRuntimeService(@Qualifier("processEngine") engine: ProcessEngine): RuntimeService = {

    engine.getRuntimeService()
  }


}
