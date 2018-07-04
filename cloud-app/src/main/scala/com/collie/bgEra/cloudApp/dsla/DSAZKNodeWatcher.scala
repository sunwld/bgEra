package com.collie.bgEra.cloudApp.dsla

import java.util.concurrent.CountDownLatch

import org.apache.zookeeper.Watcher.Event.EventType
import org.apache.zookeeper.{WatchedEvent, Watcher}


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


