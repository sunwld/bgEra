package com.collie.bgEra.cloudApp.appm

import com.collie.bgEra.cloudApp.utils.ContextHolder
import org.apache.zookeeper.{CreateMode, KeeperException}


class DistributedServiceLatchArbitrator private(val projectName: String) {

  private val rootPath: String = "/appm"
  private val latchPath: String = s"$rootPath/latch"

  private var zkDriver: ZookeeperDriver = null


  private def waitingForZookeeperRecover() = {
    if (zkDriver == null) {
      throw new AppClusterFatalException("there is no ZookeeperDriver in spring, check your config file.")
    }

    while (!zkDriver.isConnected()) {
      Thread.sleep(3000)
    }
  }

  def initZookeeperForDSA(): Unit = {
    zkDriver = ContextHolder.getBean(classOf[ZookeeperDriver])
    waitingForZookeeperRecover()
    zkDriver.createNode(rootPath, "", CreateMode.PERSISTENT)
    zkDriver.createNode(latchPath, "", CreateMode.PERSISTENT)
  }

  def grabLatch(latchKey: String) = {
    //创建node
    var latchKeyPath = s"$latchPath/$latchKey"
    try {
      zkDriver.createUnexistsNode(latchKeyPath, "", CreateMode.EPHEMERAL)
    } catch {
      case ex: KeeperException.NodeExistsException => {
        // node exists

      }
    }

    //
  }

  def releaseLatch(latchKey: String) = {

  }
}

object DistributedServiceLatchArbitrator {

  private var distributedServiceLatchArbitrator: DistributedServiceLatchArbitrator = _

  def apply(projectName: String): DistributedServiceLatchArbitrator = {
    if (distributedServiceLatchArbitrator == null) {
      this.synchronized {
        if (distributedServiceLatchArbitrator == null) {
          distributedServiceLatchArbitrator = new DistributedServiceLatchArbitrator(projectName)
        }
      }
    }
    distributedServiceLatchArbitrator
  }

  def apply() = {
    distributedServiceLatchArbitrator
  }
}