package com.collie.bgEra.cloudApp.appm.watchers

import java.text.SimpleDateFormat
import java.util.Date

import com.collie.bgEra.cloudApp.appm.{AppClusterFatalException, ZApplicationManager}
import org.apache.zookeeper.Watcher.Event.EventType
import org.apache.zookeeper.{WatchedEvent, Watcher}

object ClusterVersionWatcher extends Watcher {
  override def process(event: WatchedEvent): Unit = {
    this.synchronized{
      println(s"[${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}]ClusterVersionWatcher: watched event [$event]")
      if(event.getType == EventType.NodeDataChanged) {
        try {
          ZApplicationManager().watchClusterVersion()

          if (ZApplicationManager().getClusterStatus().equals("ACTIVE")) {
            //停止业务，重构业务，启动业务
            println(s"[${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}]ClusterVersionWatcher: cluster is active,will reconstruction service!")
            ZApplicationManager().appSuspend()
            ZApplicationManager().appReconstruction()
            ZApplicationManager().appResume()
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
