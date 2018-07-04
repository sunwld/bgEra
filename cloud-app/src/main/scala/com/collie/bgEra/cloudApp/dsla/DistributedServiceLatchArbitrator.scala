package com.collie.bgEra.cloudApp.dsla

import java.util.concurrent.CountDownLatch

import com.collie.bgEra.cloudApp.appm.AppClusterFatalException
import com.collie.bgEra.cloudApp.base.ZookeeperDriver
import com.collie.bgEra.cloudApp.utils.ContextHolder
import org.apache.zookeeper.{CreateMode, KeeperException}
import org.slf4j.{Logger, LoggerFactory}


class DistributedServiceLatchArbitrator private(val projectName: String) {
  private val logger: Logger = LoggerFactory.getLogger("appm")

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

  def grabLatch(latchKey: String): String = {
    //创建node
    var latchKeyPath = s"$latchPath/$latchKey"
    try {
      zkDriver.createUnexistsNode(latchKeyPath, "", CreateMode.EPHEMERAL)
      val latchId = latchKeyPath + System.currentTimeMillis()
      zkDriver.setData(latchKeyPath,latchId)
      latchId
    } catch {
      case ex: KeeperException.NodeExistsException => {
        Thread.sleep((new util.Random).nextInt(100))
        if(zkDriver.exists(latchKeyPath) == null){
          grabLatch(latchKey)
        }else{
          val countDownLatch = new CountDownLatch(1)
          // node not exists
          if( zkDriver.existsAndWatch(latchKeyPath,DSAZKNodeWatcher(countDownLatch,latchKeyPath)) == null ){
            grabLatch(latchKey)
          }else{
            countDownLatch.await()
            grabLatch(latchKey)
          }
        }
      }
    }
  }

  def releaseLatch(latchKey: String,latchId: String) = {
    var latchKeyPath = s"$latchPath/$latchKey"
    zkDriver.deleteNodeSafely(latchKeyPath,latchId)
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