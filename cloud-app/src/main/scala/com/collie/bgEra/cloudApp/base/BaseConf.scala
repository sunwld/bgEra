package com.collie.bgEra.cloudApp.base

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}

@Configuration
@ComponentScan(Array("com.collie.bgEra.cloudApp.base", "com.collie.bgEra.cloudApp.utils"))
class BaseConf {
}
