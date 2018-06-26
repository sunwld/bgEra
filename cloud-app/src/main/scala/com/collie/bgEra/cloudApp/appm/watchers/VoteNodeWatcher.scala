package com.collie.bgEra.cloudApp.appm.watchers

import java.text.SimpleDateFormat
import java.util.Date

import com.collie.bgEra.cloudApp.appm.{AppClusterFatalException, ZApplicationManager}
import org.apache.zookeeper.Watcher.Event.EventType
import org.apache.zookeeper.{WatchedEvent, Watcher}

object VoteNodeWatcher extends Watcher{

  override def process(event: WatchedEvent): Unit = {
    println(s"[${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}]PreNodeWatcher: watched event [$event]")
    this.synchronized{

      if(event.getType == EventType.NodeChildrenChanged) {
        Thread.sleep(5000)

        println(s"[${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}]PreNodeWatcher: my slave have changed!i will rebalance cluster.")
        try {
          ZApplicationManager().rebalanceClusterByMaster()
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
