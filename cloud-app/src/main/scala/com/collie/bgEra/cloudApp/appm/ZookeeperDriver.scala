package com.collie.bgEra.cloudApp.appm

import java.util
import java.util.concurrent.CountDownLatch

import com.collie.bgEra.cloudApp.appm.watchers.ConnectionWatcher
import com.collie.bgEra.cloudApp.utils.ContextHolder
import org.apache.zookeeper.ZooDefs.Ids
import org.apache.zookeeper._
import org.apache.zookeeper.data.Stat

class ZookeeperDriver {

  //    def apply: ZookeeperDriver = new ZookeeperDriver()
  private var zk: ZooKeeper = _
  var zkUrl : String = _

  var connectedSignal: CountDownLatch = _
  val SESSION_TIMEOUT = 5000

  val RETRY_INTERVAL = 500
  val MAX_RETRY_TIMES = 10
  var retryTimes = 0

  implicit def str2Byte(str: String) = {
    if (str == null) {
      "".getBytes("UTF-8")
    } else {
      str.getBytes("UTF-8")
    }
  }

  implicit def byte2Str(b: Array[Byte]): String = {
    new String(b, "UTF-8")
  }

  def exists(path: String, retryTimes: Int = MAX_RETRY_TIMES): Stat = {
    try {
      zk.exists(path, false)
    } catch {
      case ex: KeeperException => {
        if (retryTimes == 0) {
          throw new AppClusterFatalException()
        }
        Thread.sleep(RETRY_INTERVAL)
        exists(path, retryTimes - 1)
      }
    }

  }

  def existsAndWatch(path: String, watcher: Watcher, retryTimes: Int = MAX_RETRY_TIMES): Stat = {
    try {
      zk.exists(path, watcher)
    } catch {
      case ex: KeeperException => {
        if (retryTimes == 0) {
          throw new AppClusterFatalException()
        }
        Thread.sleep(RETRY_INTERVAL)
        existsAndWatch(path, watcher, retryTimes - 1)
      }
    }
  }

  def deleteNode(path: String, retryTimes: Int = MAX_RETRY_TIMES): Unit = {
    try {
      zk.delete(path, -1)
    }
    catch {
      case ex: KeeperException.NoNodeException => {
        println(s"zk.delete[KeeperException]:the node $path is not exists.")
      }
      case ex: KeeperException => {
        if (retryTimes == 0) {
          throw new AppClusterFatalException()
        }
        Thread.sleep(RETRY_INTERVAL)
        deleteNode(path, retryTimes - 1)
      }
    }
  }

  def createNode(path: String, data: String, createMode: CreateMode, retryTimes: Int = MAX_RETRY_TIMES): String = {
    if (zk.exists(path, false) == null) {
      try {
        zk.create(path, data, Ids.OPEN_ACL_UNSAFE, createMode)
      } catch {
        case ex: KeeperException.NodeExistsException => {
          println(s"zk.create[KeeperException.NodeExistsException]:${path} node exists")
          path
        }
        case ex: KeeperException => {
          if (retryTimes == 0) {
            throw new AppClusterFatalException()
          }
          Thread.sleep(RETRY_INTERVAL)
          createNode(path, data, createMode, retryTimes - 1)
        }
      }
    } else {
      path
    }
  }

  def getChildren(path: String, retryTimes: Int = MAX_RETRY_TIMES): util.List[String] = {
    try {
      zk.getChildren(path, false)
    } catch {
      case ex: KeeperException.NoNodeException => {
        println(s"zk.getChildren[KeeperException.NoNodeException]:the node $path is not exists.")
        new util.ArrayList[String]()
      }
      case ex: KeeperException => {
        if (retryTimes == 0) {
          throw new AppClusterFatalException()
        }
        Thread.sleep(RETRY_INTERVAL)
        getChildren(path, retryTimes - 1)
      }
    }
  }

  def getChildrenAndWatch(path: String, watcher: Watcher, retryTimes: Int = MAX_RETRY_TIMES): util.List[String] = {
    try {
      zk.getChildren(path, watcher)
    } catch {
      case ex: KeeperException.NoNodeException => {
        println(s"zk.getChildren[KeeperException.NoNodeException]:the node $path is not exists.")
        new util.ArrayList[String]()
      }
      case ex: KeeperException => {
        if (retryTimes == 0) {
          throw new AppClusterFatalException()
        }
        Thread.sleep(RETRY_INTERVAL)
        getChildrenAndWatch(path, watcher, retryTimes - 1)
      }
    }
  }

  def setData(path: String, data: String, retryTimes: Int = MAX_RETRY_TIMES): Unit = {
    try {
      zk.setData(path, data, -1)
    } catch {
      case ex: KeeperException.NoNodeException => {
        println(s"zk.getChildren[KeeperException.NoNodeException]:the node $path is not exists.")
      }
      case ex: KeeperException => {
        if (retryTimes == 0) {
          throw new AppClusterFatalException()
        }
        Thread.sleep(RETRY_INTERVAL)
        setData(path, data, retryTimes - 1)
      }
    }
  }

  def getData(path: String, retryTimes: Int = MAX_RETRY_TIMES): String = {
    try {
      byte2Str(zk.getData(path, false, null))
    } catch {
      case ex: KeeperException.NoNodeException => {
        println(s"zk.getChildren[KeeperException.NoNodeException]:the node $path is not exists.")
        ""
      }
      case ex: KeeperException => {
        if (retryTimes == 0) {
          throw new AppClusterFatalException()
        }
        Thread.sleep(RETRY_INTERVAL)
        getData(path, retryTimes - 1)
      }
    }
  }

  def getDataAndWatch(path: String, watcher: Watcher, retryTimes: Int = MAX_RETRY_TIMES): String = {
    try {
      byte2Str(zk.getData(path, watcher, null))
    } catch {
      case ex: KeeperException.NoNodeException => {
        println(s"zk.getChildren[KeeperException.NoNodeException]:the node $path is not exists.")
        ""
      }
      case ex: KeeperException => {
        if (retryTimes == 0) {
          throw new AppClusterFatalException()
        }
        Thread.sleep(RETRY_INTERVAL)
        getDataAndWatch(path, watcher, retryTimes - 1)
      }
    }
  }

  def getSessionId() = {
    zk.getSessionId
  }

  def connectZK(zkUrl: String): Unit = {
    this.zkUrl = zkUrl
    if (zk == null || !zk.getState.isConnected) {
      this.synchronized {
        if (zk == null || !zk.getState.isConnected) {
          connectedSignal = new CountDownLatch(1)
          zk = new ZooKeeper(zkUrl, SESSION_TIMEOUT, ConnectionWatcher(connectedSignal))
          connectedSignal.await()
        }
      }
    }
  }
  def connectZK(): Unit = {
    connectZK(this.zkUrl)
  }

  def close() = {
    zk.close()
    zk = null
  }
}


