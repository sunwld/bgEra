package com.collie.bgEra.cloudApp.appm.watchers

import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.CountDownLatch

import com.collie.bgEra.cloudApp.appm.{ZApplicationManager, ZookeeperDriver}
import org.apache.zookeeper.Watcher.Event.KeeperState
import org.apache.zookeeper.{WatchedEvent, Watcher}

/**
  *
  *
  *
  *
  * */
class ConnectionWatcher private () extends  Watcher{
  private var connectedSignal:CountDownLatch = _
  override def process(event: WatchedEvent): Unit = {
    println(s"[${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}]ConnectionWatcher: watched event [$event]")
    if(event.getState == KeeperState.SyncConnected){
      connectedSignal.countDown()
    }else if( event.getState == KeeperState.Disconnected ||
            event.getState == KeeperState.Expired||
            event.getState == KeeperState.AuthFailed ){

      println(s"[${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}]ConnectionWatcher: connect is break!will reimplement cluster.")
      ZApplicationManager().reImplementZManagement()
    }
  }
}

object ConnectionWatcher{
  private var connectionWatcher:ConnectionWatcher = new ConnectionWatcher()

  def apply(connectedSignal:CountDownLatch) = {
    connectionWatcher.connectedSignal = connectedSignal
    connectionWatcher
  }
}