package com.collie.bgEra.cloudApp.appm.watchers

import com.collie.bgEra.cloudApp.appm.{AppClusterFatalException, ZApplicationManager}
import org.apache.zookeeper.Watcher.Event.EventType
import org.apache.zookeeper.{WatchedEvent, Watcher}
import org.slf4j.{Logger, LoggerFactory}

object VoteNodeWatcher extends Watcher{
  private val logger: Logger = LoggerFactory.getLogger("appm")

  override def process(event: WatchedEvent): Unit = {
    logger.info(s"PreNodeWatcher: watched event [$event]")
    this.synchronized{

      if(event.getType == EventType.NodeChildrenChanged) {
        Thread.sleep(5000)

        logger.warn(s"PreNodeWatcher: my slave have changed!i will rebalance cluster.")
        try {
          ZApplicationManager().rebalanceClusterByMaster()
        } catch {
          case ex: AppClusterFatalException => {
            logger.error(s"ERROR!!!recoverFromFatalError:cluster reconstraction!",ex)
            ZApplicationManager().recoverFromFatalError(ex)
          }
        }
      }
    }
  }
}
