package com.collie.bgEra.cloudApp.dtsf.conf

import com.alibaba.druid.pool.DruidDataSource
import com.collie.bgEra.cloudApp.CloudAppContext
import com.collie.bgEra.cloudApp.appm.conf.AppmConf
import com.collie.bgEra.cloudApp.dtsf.DistributedTaskBus
import com.collie.bgEra.cloudApp.redisCache.conf.RedisCacheConf
import com.collie.bgEra.cloudApp.utils.ContextHolder
import com.collie.bgEra.commons.util.CommonUtils
import org.mybatis.spring.SqlSessionFactoryBean
import org.quartz.CronTrigger
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Import}
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.scheduling.quartz.{CronTriggerFactoryBean, MethodInvokingJobDetailFactoryBean, SchedulerFactoryBean}

/**
  * Dtsf组件实现分布式任务调度，
  * 具体任务可以拆分为任务单元链，单个可执行的任务程序为任务单元，
  * Dtsf可以按照具体的TARGET进行分片（SHARDING)，
  * 通过指定TARGET的SHARDINGINDEX控制分片，
  * Dtsf加载任务需要从数据库中读取，目前仅支持MYSQL数据库，
  * 故Dtsf强依赖一个MYSQL的数据源
  * 依赖SPRING注入： dtfsDataSource:DruidDataSource
  * Dtsf依赖 APPM组件实现云功能，本能依赖APPM所依赖的类
  * AppmContext,AppManagerStandardSkill
  * Dtsf优化性能将大部分数据实现了动态缓存，依赖了redisCache组件
  * redisCache要求提供jedisCluster
  * 总结：需要使用DTSF调度任务的模块需要将以下组件注册到SPRING中
  * MYSQL数据源：dtfsDataSource：DruidDataSource
  * APPM配置信息：appmContext：AppmContext
  * AppManagerStandardSkill实现类： appManagerStandardSkill：AppManagerStandardSkill
  * redis数据源：jedisCluster：JedisCluster
  */
@Configuration
@Import(Array(classOf[AppmConf],classOf[RedisCacheConf]))
@ComponentScan(Array("com.collie.bgEra.cloudApp.dtsf"))
class DtsfConf {

  @Bean(Array("bgEra_dtsf_SqlSessionFactory"))
  def sqlSeesionFatory(@Qualifier("dtfsDataSource") dtfsDataSource: DruidDataSource): SqlSessionFactoryBean = {
    val sqlSessionFactoryBean = new SqlSessionFactoryBean()
    sqlSessionFactoryBean.setDataSource(dtfsDataSource)
    val resolver = new PathMatchingResourcePatternResolver()
    val resources = resolver.getResources("classpath*:com/collie/bgEra/cloudApp/dtsf/mapper/*Mapper.xml")
    sqlSessionFactoryBean.setMapperLocations(resources)
    ContextHolder.getBean(classOf[CloudAppContext]).putSqlSessionFactory("dtsfMain",sqlSessionFactoryBean.getObject())
    sqlSessionFactoryBean
  }


  @Bean(name = Array("mainJobDetail"))
  def jobDetail(@Qualifier("distributedTaskBus") bus: DistributedTaskBus): MethodInvokingJobDetailFactoryBean = {
    val jobDetailFactoryBean: MethodInvokingJobDetailFactoryBean = new MethodInvokingJobDetailFactoryBean()
    jobDetailFactoryBean.setConcurrent(false)
    jobDetailFactoryBean.setName("mainJobDetail")
    jobDetailFactoryBean.setGroup("mainGroup")
    jobDetailFactoryBean.setTargetObject(bus)
    jobDetailFactoryBean.setTargetMethod("runBus")
    jobDetailFactoryBean
  }
  @Bean(name = Array("mainTrigger"))
  def mainJobTrigger(@Qualifier("mainJobDetail") mainJobDetail: MethodInvokingJobDetailFactoryBean): CronTriggerFactoryBean = {
    val trigger = new CronTriggerFactoryBean()
    trigger.setJobDetail(mainJobDetail.getObject)
    trigger.setCronExpression("0/2 * * * * ?")
    trigger.setName("mainTrigger")
    trigger.setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING)
    trigger
  }

  @Bean(Array("mainScheduler"))
  def mainScheduler(@Qualifier("mainTrigger") mainTrigger: CronTriggerFactoryBean): SchedulerFactoryBean = {
    val scheduler = new SchedulerFactoryBean()
    scheduler.setTriggers(mainTrigger.getObject)
    scheduler.setAutoStartup(false)
    scheduler.setStartupDelay(5)
    val mainScheduler = CommonUtils.readPropertiesFile("schedulerProp/mainScheduler.properties")
    scheduler.setQuartzProperties(mainScheduler)
    scheduler
  }

}
