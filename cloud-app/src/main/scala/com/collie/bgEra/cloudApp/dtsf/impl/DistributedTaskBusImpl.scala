package com.collie.bgEra.cloudApp.dtsf.impl

import com.collie.bgEra.cloudApp.appm.ZApplicationManager
import com.collie.bgEra.cloudApp.dtsf.DistributedTaskBus
import javax.sql.DataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component("distributedTaskBus")
class DistributedTaskBusImpl extends DistributedTaskBus {
    override def runBus(): Unit = ???
}
