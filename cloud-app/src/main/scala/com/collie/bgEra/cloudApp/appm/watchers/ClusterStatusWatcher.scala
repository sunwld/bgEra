package com.collie.bgEra.cloudApp.appm.watchers

import java.text.SimpleDateFormat
import java.util.Date

import com.collie.bgEra.cloudApp.appm.{AppClusterFatalException, ZApplicationManager}
import org.apache.zookeeper.Watcher.Event.EventType
import org.apache.zookeeper.{WatchedEvent, Watcher}

object ClusterStatusWatcher extends Watcher {
  override def process(event: WatchedEvent): Unit = {
    this.synchronized {
      println(s"[${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}]ClusterStatusWatcher: watched event [$event]")


      try {
        ZApplicationManager().watchStatusNode()
        //      if(event.getType == EventType.NodeCreated){
        //          //ZApplicationManager().appManagerStandardSkill.resume()
        //      }else
        if (event.getType == EventType.NodeDeleted) {
          println(s"[${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}]ClusterStatusWatcher: appm will suspend service!")
          ZApplicationManager().appManagerStandardSkill.suspend()
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
