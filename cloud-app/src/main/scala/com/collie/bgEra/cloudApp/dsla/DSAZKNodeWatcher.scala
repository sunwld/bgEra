package com.collie.bgEra.cloudApp.dsla

import java.util.concurrent.CountDownLatch

import org.apache.zookeeper.Watcher.Event.EventType
import org.apache.zookeeper.{WatchedEvent, Watcher}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable


class DSAZKNodeWatcher private() extends Watcher {
  private val logger: Logger = LoggerFactory.getLogger("dsla")

  private var latchBeanList: mutable.ListBuffer[LatchBean] = _


  override def process(event: WatchedEvent): Unit = {
    if (event.getType == EventType.NodeDeleted) {
      latchBeanList.synchronized {
        for(i<- 0 until latchBeanList.length){
          val latch = latchBeanList.remove(0)
          latch.countDownLatch.countDown()
          logger.info(s"wathed NodeDeleted: countdown latch:${latch.latchKey}")
        }
      }
    }
  }
}

object DSAZKNodeWatcher {
  private var dsaZKNodeWatcher: DSAZKNodeWatcher = _

  def apply(countDownLatch: CountDownLatch, latchKey: String) = {

    if (dsaZKNodeWatcher == null) {
      this.synchronized {
        if (dsaZKNodeWatcher == null) {
          dsaZKNodeWatcher = new DSAZKNodeWatcher()
          dsaZKNodeWatcher.latchBeanList = mutable.ListBuffer()
        }
      }
    }

    dsaZKNodeWatcher.latchBeanList.synchronized {
      dsaZKNodeWatcher.latchBeanList.append(new LatchBean(latchKey, countDownLatch))
    }

    dsaZKNodeWatcher
  }
}


