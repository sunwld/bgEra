package com.collie.bgEra.cloudApp.base

import java.util.concurrent.CountDownLatch
import org.apache.zookeeper.Watcher.Event.KeeperState
import org.apache.zookeeper.{WatchedEvent, Watcher}
import org.slf4j.{Logger, LoggerFactory}

class ConnectionWatcher private() extends Watcher {
  private var connectedSignal: CountDownLatch = _
  private var zkSession: ZookeeperSession = null
  private val logger: Logger = LoggerFactory.getLogger("appm")

  override def process(event: WatchedEvent): Unit = {
    logger.info(s"ConnectionWatcher: watched event [$event]")
    if (event.getState == KeeperState.SyncConnected) {
      connectedSignal.countDown()
    } else if (event.getState == KeeperState.Disconnected ||
      event.getState == KeeperState.Expired ||
      event.getState == KeeperState.AuthFailed) {

      logger.error(s"ConnectionWatcher: connect is break!will reimplement cluster.")
      zkSession.connectZK()
    }
  }
}

object ConnectionWatcher {
  def apply(connectedSignal: CountDownLatch, zkSession: ZookeeperSession) = {
    val connectionWatcher: ConnectionWatcher = new ConnectionWatcher()
    connectionWatcher.connectedSignal = connectedSignal
    connectionWatcher.zkSession = zkSession
    connectionWatcher
  }
}