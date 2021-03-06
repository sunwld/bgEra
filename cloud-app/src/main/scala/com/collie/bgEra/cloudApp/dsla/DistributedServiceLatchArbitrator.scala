package com.collie.bgEra.cloudApp.dsla

import java.util.concurrent.{CountDownLatch, TimeUnit}

import com.collie.bgEra.cloudApp.base.ZookeeperSession
import com.collie.bgEra.cloudApp.utils.ContextHolder
import org.apache.zookeeper.{CreateMode, KeeperException}
import org.slf4j.{Logger, LoggerFactory}


class DistributedServiceLatchArbitrator private(val projectName: String) {
  private val logger: Logger = LoggerFactory.getLogger("dsla")

  private val rootPath: String = "/appm"
  private val latchPath: String = s"$rootPath/latch"

  private var zkSession: ZookeeperSession = null

  def initZookeeperForDSA(zkSession: ZookeeperSession): Unit = {
    zkSession.createNode(rootPath, "", CreateMode.PERSISTENT)
    zkSession.createNode(latchPath, "", CreateMode.PERSISTENT)
  }

  def grabLatch(latchKey: String): String = {
    //创建node
    var latchKeyPath = s"$latchPath/$latchKey"
    val zkSessionId: Long = zkSession.getSessionId()
    try {
      zkSession.createUnexistsNode(latchKeyPath, "", CreateMode.EPHEMERAL)
      val latchId = s"$latchKeyPath,latchTime:${System.currentTimeMillis()},zkSession:${zkSessionId}"
      zkSession.setData(latchKeyPath, latchId)
      logger.debug(s"session: $zkSessionId get latch $latchKeyPath")
      latchId
    } catch {
      case ex: KeeperException.NodeExistsException => {
        Thread.sleep((new util.Random).nextInt(200))
        if (zkSession.exists(latchKeyPath) == null) {
          grabLatch(latchKey)
        } else {
          val countDownLatch = new CountDownLatch(1)
          // node not exists
          if (zkSession.existsAndWatch(latchKeyPath, DSAZKNodeWatcher(countDownLatch, latchKeyPath)) == null) {
            grabLatch(latchKey)
          } else {
            logger.debug(latchKey + "will sleep for watcher")
            countDownLatch.await(10, TimeUnit.SECONDS)
            logger.debug(latchKey + "end sleep")
            grabLatch(latchKey)
          }
        }
      }
    }
  }

  def releaseLatch(latchKey: String, latchId: String) = {
    var latchKeyPath = s"$latchPath/$latchKey"
    zkSession.deleteNodeSafely(latchKeyPath, latchId)
    logger.debug(s"session: ${zkSession.getSessionId()} release latch $latchKeyPath")
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