package com.collie.bgEra.opdc

import com.alibaba.druid.pool.DruidDataSource
import javax.sql.DataSource
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.web.servlet.{View, ViewResolver}
import org.springframework.web.servlet.config.annotation._
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import org.springframework.web.servlet.view.freemarker.{FreeMarkerConfigurer, FreeMarkerViewResolver}
//import org.springframework.cloud.client.discovery.EnableDiscoveryClient
//import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.context.annotation._

@SpringBootApplication(scanBasePackages = Array("com.collie.bgEra"))
//@EnableEurekaClient
//@EnableDiscoveryClient
class Config extends WebMvcConfigurationSupport{
    @Bean(name = Array("dataSource"))
    @ConfigurationProperties(prefix = "spring.datasource.druid")
    def dataSource : DataSource = {
        DataSourceBuilder.create().`type`(classOf[DruidDataSource]).build()
    }

    override def addViewControllers(registry: ViewControllerRegistry): Unit = {
        registry.addViewController("/home").setViewName("opdcHome")
    }

    override def addResourceHandlers(registry: ResourceHandlerRegistry): Unit = {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/")
    }


//    @Bean
//    def freeMarkerViewResolver(): FreeMarkerViewResolver = {
//        val viewResolver = new FreeMarkerViewResolver()
//        // freemarker本身配置了templateLoaderPath而在viewResolver中不需要配置prefix，且路径前缀必须配置在 templateLoaderPath 中
//        viewResolver.setPrefix("")
//        viewResolver.setSuffix(".ftl")
//        viewResolver.setCache(false)
//        viewResolver.setContentType("text/html;charset=UTF-8")
//        viewResolver.setRequestContextAttribute("requestContext") //为模板调用时，调用 request 对象的变量名
//        viewResolver.setExposeRequestAttributes(true);
//        viewResolver.setExposeSessionAttributes(true);
//        viewResolver
//    }
//
//    @Bean
//    def freemarkerConfig(): FreeMarkerConfigurer = {
//        val freemarkerConfig = new FreeMarkerConfigurer()
//        freemarkerConfig.setDefaultEncoding("UTF-8")
//        freemarkerConfig.setTemplateLoaderPath("classpath:/templates/")
//        var configuration: template.Configuration = null
//        configuration = freemarkerConfig.createConfiguration()
//        configuration.setTemplateUpdateDelayMilliseconds(0)
//        configuration.setDefaultEncoding("UTF-8")
//        freemarkerConfig
//    }
}

object Application extends App{
    SpringApplication.run(classOf[Config])
}

