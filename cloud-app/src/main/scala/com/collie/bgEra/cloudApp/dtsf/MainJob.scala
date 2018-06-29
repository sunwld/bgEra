package com.collie.bgEra.cloudApp.dtsf

import java.util.Date

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.stereotype.Component

@Component("mainJob")
@EnableScheduling
class MainJob {

  def execu(): Unit ={
    println("===============================")
  }
}
