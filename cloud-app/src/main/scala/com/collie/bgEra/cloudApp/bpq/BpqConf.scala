package com.collie.bgEra.cloudApp.bpq

import com.collie.bgEra.cloudApp.CloudAppContext
import com.collie.bgEra.cloudApp.redisCache.conf.RedisCacheConf
import com.collie.bgEra.cloudApp.utils.ContextHolder
import org.quartz.Scheduler
import org.quartz.impl.StdSchedulerFactory
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.context.annotation._
import org.springframework.scheduling.quartz.{CronTriggerFactoryBean, SchedulerFactoryBean}


/**
  * 批量数据持久化队列功能模块：
  *
  * @author yjd swl
  * @todo
  *
  * @note 初始化队列时，需要传入
  *       id : 根据此ID初始化queue对象，相同id的 持久化请求将放在同一队列中，使用同一个sqlSessionFactory执行sql语句
  *       sqlSessionFactory对象
  *       sql statement 例如："com.collie.bgEra.cloudApp.dtsf.mapper.TaskMapper.qryAllTartInfo"
  */
@Configuration
@ComponentScan(basePackageClasses = Array(classOf[BpqQueueManger],classOf[ContextHolder]))
@Import(Array(classOf[RedisCacheConf]))
class BpqConf {
  @Bean(Array("bpqQueueScheduler"))
  def bpqQueueScheduler(@Autowired context: CloudAppContext): Scheduler = {
    val sf = new StdSchedulerFactory()

    val prop = context.getQuartzSchedulerPorp("template")
    prop.setProperty("org.quartz.scheduler.instanceName","bpqQueueScheduler")
    prop.setProperty("org.quartz.threadPool.threadCount","10")
    sf.initialize(prop)
    val scheduler = sf.getScheduler()
    scheduler.start()
    scheduler
  }



}
