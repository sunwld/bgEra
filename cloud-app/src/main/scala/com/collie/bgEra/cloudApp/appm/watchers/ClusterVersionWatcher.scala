package com.collie.bgEra.cloudApp.appm.watchers

import com.collie.bgEra.cloudApp.appm.{AppClusterFatalException, ZApplicationManager}
import org.apache.zookeeper.Watcher.Event.EventType
import org.apache.zookeeper.{WatchedEvent, Watcher}
import org.slf4j.{Logger, LoggerFactory}

object ClusterVersionWatcher extends Watcher {
  private val logger: Logger = LoggerFactory.getLogger("appm")
  override def process(event: WatchedEvent): Unit = {
//    this.synchronized{
//      logger.info(s"ClusterVersionWatcher: watched event [$event]")
//      if(event.getType == EventType.NodeDataChanged) {
//        try {
//          ZApplicationManager().watchClusterVersion()
//
//          if (ZApplicationManager().getClusterStatus().equals("ACTIVE")) {
//            //停止业务，重构业务，启动业务
//            logger.warn(s"ClusterVersionWatcher: cluster is active,will reconstruction service!")
//            ZApplicationManager().appSuspend()
//            ZApplicationManager().appReconstruction()
//            ZApplicationManager().appResume()
//          }
//        } catch {
//          case ex: AppClusterFatalException => {
//            logger.error(s"ERROR!!!recoverFromFatalError:cluster reconstraction!",ex)
//            ZApplicationManager().recoverFromFatalError(ex)
//          }
//        }
//      }
//    }
  }
}
