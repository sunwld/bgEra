package com.collie.bgEra.cloudApp.dtsf.impl

import com.collie.bgEra.cloudApp.dtsf.DistributedTaskBus
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component

@Component("distributedTaskBus")
@EnableScheduling
class DistributedTaskBusImpl extends DistributedTaskBus {

    private val logger: Logger = LoggerFactory.getLogger("dtsf")
    override def runBus(): Unit = {

    }
}
