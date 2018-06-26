package com.collie.bgEra.cloudApp.appm.watchers

import java.text.SimpleDateFormat
import java.util.Date

import com.collie.bgEra.cloudApp.appm.{AppClusterFatalException, ZApplicationManager}
import org.apache.zookeeper.Watcher.Event.EventType
import org.apache.zookeeper.{WatchedEvent, Watcher}

object PreNodeWatcher extends Watcher{
  override def process(watchedEvent: WatchedEvent): Unit = {
    this.synchronized{
      println(s"[${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}]PreNodeWatcher: watched event [$watchedEvent]")
      if(watchedEvent.getType == EventType.NodeDeleted) {
        try {
          Thread.sleep(5000)
          ZApplicationManager().watchVoteNode()

          if (ZApplicationManager().isMaster()) {
            println(s"[${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}]PreNodeWatcher: i stop my service, i am the new master,i will rebalance cluster!")
            ZApplicationManager().inAcvtiveCluster()
            ZApplicationManager().appSuspend()
            ZApplicationManager().rebalanceClusterByMaster()
          }
        } catch {
          case ex: AppClusterFatalException => {
            println(s"[${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}]ERROR!!!recoverFromFatalError:cluster reconstraction!")
            ex.printStackTrace()
            ZApplicationManager().recoverFromFatalError(ex)
          }
        }
      }
    }
  }
}
