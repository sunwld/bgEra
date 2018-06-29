package com.collie.bgEra.opdc

import com.collie.bgEra.opdcConf.ConfToDtsf
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.servlet.config.annotation._
import org.springframework.context.annotation._

//@EnableEurekaClient
//@EnableDiscoveryClient
@Import(Array(classOf[ConfToDtsf]))
@SpringBootApplication(scanBasePackages = Array("com.collie.bgEra.opdc"))
class Config extends WebMvcConfigurationSupport{
//    @Bean(name = Array("dataSource"))
//    @ConfigurationProperties(prefix = "spring.datasource.druid")
//    def dataSource : DataSource = {
//        DataSourceBuilder.create().`type`(classOf[DruidDataSource]).build()
//    }

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

