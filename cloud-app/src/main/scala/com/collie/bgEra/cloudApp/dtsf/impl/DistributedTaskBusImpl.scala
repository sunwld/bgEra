package com.collie.bgEra.cloudApp.dtsf.impl

import com.collie.bgEra.cloudApp.dtsf.DistributedTaskBus
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component

@Component("distributedTaskBus")
@EnableScheduling
class DistributedTaskBusImpl extends DistributedTaskBus {
    override def runBus(): Unit = ???
}
