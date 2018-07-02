package com.collie.bgEra.cloudApp.appm.watchers

import java.util.concurrent.CountDownLatch

import com.collie.bgEra.cloudApp.appm.{ZApplicationManager, ZookeeperDriver}
import com.collie.bgEra.cloudApp.utils.ContextHolder
import org.apache.zookeeper.Watcher.Event.KeeperState
import org.apache.zookeeper.{WatchedEvent, Watcher}
import org.slf4j.{Logger, LoggerFactory}

/**
  *
  *
  *
  *
  * */
class ConnectionWatcher private () extends  Watcher{
  private var connectedSignal:CountDownLatch = _
  private var logger: Logger = _

  override def process(event: WatchedEvent): Unit = {
    logger.info(s"ConnectionWatcher: watched event [$event]")
    if(event.getState == KeeperState.SyncConnected){
      connectedSignal.countDown()
    }else if( event.getState == KeeperState.Disconnected ||
            event.getState == KeeperState.Expired||
            event.getState == KeeperState.AuthFailed ){

      logger.error(s"ConnectionWatcher: connect is break!will reimplement cluster.")
      ContextHolder.getBean(classOf[ZookeeperDriver]).connectZK()
    }
  }
}

object ConnectionWatcher{
  private var connectionWatcher:ConnectionWatcher = new ConnectionWatcher()
  connectionWatcher.logger = LoggerFactory.getLogger("appm")

  def apply(connectedSignal:CountDownLatch) = {
    connectionWatcher.connectedSignal = connectedSignal
    connectionWatcher
  }
}