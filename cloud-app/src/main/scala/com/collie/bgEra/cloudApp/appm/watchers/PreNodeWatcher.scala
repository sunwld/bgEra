package com.collie.bgEra.cloudApp.appm.watchers

import com.collie.bgEra.cloudApp.appm.{AppClusterFatalException, ZApplicationManager}
import org.apache.zookeeper.Watcher.Event.EventType
import org.apache.zookeeper.{WatchedEvent, Watcher}
import org.slf4j.{Logger, LoggerFactory}

object PreNodeWatcher extends Watcher{
  private val logger: Logger = LoggerFactory.getLogger("appm")
  override def process(watchedEvent: WatchedEvent): Unit = {
//    this.synchronized{
//      logger.info(s"PreNodeWatcher: watched event [$watchedEvent]")
//      if(watchedEvent.getType == EventType.NodeDeleted) {
//        try {
//          Thread.sleep(5000)
//          ZApplicationManager().watchVoteNode()
//
//          if (ZApplicationManager().isMaster()) {
//            logger.info(s"PreNodeWatcher: i stop my service, i am the new master,i will rebalance cluster!")
//            ZApplicationManager().inAcvtiveCluster()
//            ZApplicationManager().appSuspend()
//            ZApplicationManager().rebalanceClusterByMaster()
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
