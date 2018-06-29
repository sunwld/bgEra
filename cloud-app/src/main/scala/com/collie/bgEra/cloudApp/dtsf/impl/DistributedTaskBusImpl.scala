package com.collie.bgEra.cloudApp.dtsf.impl

import com.collie.bgEra.cloudApp.dtsf.DistributedTaskBus
import org.springframework.stereotype.Component

@Component("distributedTaskBus")
class DistributedTaskBusImpl extends DistributedTaskBus {
    override def runBus(): Unit = ???
}
