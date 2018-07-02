package com.collie.bgEra.cloudApp.appm.watchers

import com.collie.bgEra.cloudApp.appm.{AppClusterFatalException, ZApplicationManager}
import org.apache.zookeeper.Watcher.Event.EventType
import org.apache.zookeeper.{WatchedEvent, Watcher}
import org.slf4j.{Logger, LoggerFactory}

object ClusterStatusWatcher extends Watcher {
  private val logger: Logger = LoggerFactory.getLogger("appm")
  override def process(event: WatchedEvent): Unit = {
//    this.synchronized {
//      logger.info(s"ClusterStatusWatcher: watched event [$event]")
//
//      try {
//        ZApplicationManager().watchStatusNode()
//        //      if(event.getType == EventType.NodeCreated){
//        //          //ZApplicationManager().appManagerStandardSkill.resume()
//        //      }else
//        if (event.getType == EventType.NodeDeleted) {
//          logger.warn(s"ClusterStatusWatcher: appm will suspend service!")
//          ZApplicationManager().appManagerStandardSkill.suspend(ZApplicationManager().clusterInfo)
//        }
//      } catch {
//        case ex: AppClusterFatalException => {
//          logger.error(s"ERROR!!!recoverFromFatalError:cluster reconstraction!",ex)
//          ZApplicationManager().recoverFromFatalError(ex)
//        }
//      }
//    }
  }
}
