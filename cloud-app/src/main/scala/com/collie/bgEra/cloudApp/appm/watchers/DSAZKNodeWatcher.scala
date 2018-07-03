package com.collie.bgEra.cloudApp.appm.watchers

import java.util.concurrent.CountDownLatch

import com.collie.bgEra.cloudApp.appm.{AppClusterFatalException, ZApplicationManager}
import com.collie.bgEra.cloudApp.appm.watchers.ClusterStatusWatcher.logger
import com.collie.bgEra.cloudApp.base.ConnectionWatcher.connectionWatcher
import org.apache.zookeeper.Watcher.Event.EventType
import org.apache.zookeeper.{WatchedEvent, Watcher}
import org.slf4j.{Logger, LoggerFactory}


class DSAZKNodeWatcher private(connectedSignal: CountDownLatch,latchKey: String) extends Watcher{

  override def process(event: WatchedEvent): Unit = {
    if (event.getType == EventType.NodeDeleted) {
      connectedSignal.countDown()
    }
  }
}

object DSAZKNodeWatcher{
  def apply(connectedSignal: CountDownLatch,latchKey: String) = {
    new DSAZKNodeWatcher(connectedSignal: CountDownLatch,latchKey: String)
  }
}


