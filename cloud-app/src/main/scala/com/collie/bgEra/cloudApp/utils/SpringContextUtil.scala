package com.collie.bgEra.cloudApp.utils

import org.springframework.context.{ApplicationContext, ApplicationContextAware}

object SpringContextUtil extends  ApplicationContextAware{
  var applicationContext: ApplicationContext = _
  override def setApplicationContext(applicationContext: ApplicationContext): Unit = {
    this.applicationContext = applicationContext
  }
}
