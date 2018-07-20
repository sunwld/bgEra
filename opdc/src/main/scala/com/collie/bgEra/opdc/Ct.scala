package com.collie.bgEra.opdc

import java.sql.{PreparedStatement, ResultSet}
import java.util.{Date, Properties}

import org.slf4j.{Logger, LoggerFactory}
import org.springframework.web.bind.annotation.ResponseBody
import javax.sql.DataSource
import org.springframework.web.bind.annotation.{RequestMapping, RestController}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.quartz.{MethodInvokingJobDetailFactoryBean, SchedulerFactoryBean}

@RestController
@RequestMapping(Array("/opdc"))
class Ct {
    private val logger: Logger = LoggerFactory.getLogger("opdc")

    @Autowired
    val service : TestService = null

      /**
      * 孙文龙
      * @return
      */


    @RequestMapping(Array("/q"))
    def scheduler: Any = {
        "success"
    }
}