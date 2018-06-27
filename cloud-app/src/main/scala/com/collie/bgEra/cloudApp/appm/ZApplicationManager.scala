package com.collie.bgEra.cloudApp.appm

import java.text.SimpleDateFormat
import java.util
import java.util.Date

import com.collie.bgEra.cloudApp.appm.watchers.{ClusterStatusWatcher, ClusterVersionWatcher, PreNodeWatcher, VoteNodeWatcher}
import org.apache.zookeeper.{CreateMode, KeeperException}

import scala.collection.JavaConversions._
import scala.collection.mutable


class ZApplicationManager private(
                                   val projectName: String, val zkUrls: String,
                                   val minLiveServCount: Int, val clusterInitServCount: Int,
                                   val appManagerStandardSkill: AppManagerStandardSkill) {

  private var zookeeperDriver: ZookeeperDriver.type = ZookeeperDriver
  private var voterIDs: java.util.List[String] = _
  private val MAX_RETRY_TIMES_PER_MIN: Int = 5
  private val RETRY_INTERVAL_SECONDS: Int = 1
  private var retry_times = 0


  val voteSplit = "_SEQ_"
  var projectPath = ""
  var rootPath = ""
  var votePath = ""
  var clusterStatusPath = ""
  var clusterVersionPath = ""
  var voterId: String = _
  var voterPlace: (String, String) = _
  val MASTER = "MASTER"
  val SLAVE = "SLAVE"

  private var appStatus = "INACTIVE"

  def recoverFromFatalError(fatalException: AppClusterFatalException, retryTimes: Int = MAX_RETRY_TIMES_PER_MIN): String = {
    breakConnectionFromZk()
    try {
      implementZManagement()
    } catch {
      case ex: AppClusterFatalException => {
        var retryTimesTmp = retryTimes
        if (retryTimesTmp == 0) {
          Thread.sleep(60000)
          retryTimesTmp = MAX_RETRY_TIMES_PER_MIN
        }
        Thread.sleep(RETRY_INTERVAL_SECONDS * 1000)
        recoverFromFatalError(ex, retryTimesTmp - 1)
      }
    }
  }

  def rebalanceClusterByMaster(): Unit = {
    var voterIdCount = watchVoteNode().size()
    var clusterStatus = getClusterStatus()
    val clusterInitServCount = ZApplicationManager().clusterInitServCount
    val clusterMinServCount = ZApplicationManager().minLiveServCount
    println(s"[${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}]rebalanceClusterByMaster:cluster voters count:${voterIdCount}")
    println(s"[${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}]rebalanceClusterByMaster:cluster zk current status:${clusterStatus}")
    println(s"clusterStatus : $clusterStatus")

    if ("INACTIVE".equals(clusterStatus)) {
      //集群不可用时
      if (voterIdCount >= clusterMinServCount) {
        println(s"[${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}]rebalanceClusterByMaster:voterCount bigger than:${clusterMinServCount},active cluster,relocate task,incr version")
        //先做REALOOCATE任务，开始初始化集群，修改集群状态，广播事件 建立VOTERWATCHER、达到重新分配任务、开始运行任务目的
        appReallocation()

        acvtiveCluster()
        incrClusterVersion()
      }
    } else if ("ACTIVE".equals(clusterStatus)) {
      //集群可用时
      if (voterIdCount < clusterMinServCount) {
        //修改集群状态，并通过修改集群状态为INACTIVE，广播事件，达到停止业务的目的
        println(s"[${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}]rebalanceClusterByMaster:voterCount less than:${clusterMinServCount},inactive cluster")
        inAcvtiveCluster()
      } else {
        //先做REALOOCATE任务，修改集群版本号，通过版本号广播达到让应用重新加载任务
        println(s"[${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}]rebalanceClusterByMaster:voterCount bigger than:${clusterMinServCount},relocate task,incr version")
        appReallocation()
        incrClusterVersion()
      }
    }
  }

  def implementZManagement(): String = {
    var voterId: String = ""
    try {
      println(s"[${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}]implementZManagement:conn zk,create vote,watchnode,watchstatus,watchcluster")
      zookeeperDriver.connectZK(zkUrls)
      voterId = createVoteZnode(projectName, "")
      voterIDs = watchVoteNode()
      watchStatusNode()
      watchClusterVersion()

      voterId
    } catch {
      case ex: AppClusterFatalException => {
        println(s"[${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}]ERROR!!!recoverFromFatalError:cluster reconstraction!")
        ex.printStackTrace()
        recoverFromFatalError(ex)
      }
    }
  }

  def reImplementZManagement(): Unit = {
    breakConnectionFromZk()
    implementZManagement()
  }

  private def createVoteZnode(projectName: String, voteNodeValue: String): String = {
    projectPath = "/" + projectName
    rootPath = s"$projectPath/appm"
    clusterStatusPath = s"$rootPath/status"
    votePath = s"$rootPath/vote"
    clusterVersionPath = s"$rootPath/clusterVersion"

    zookeeperDriver.createNode(projectPath, "", CreateMode.PERSISTENT)
    zookeeperDriver.createNode(rootPath, "", CreateMode.PERSISTENT)
    zookeeperDriver.createNode(votePath, "", CreateMode.PERSISTENT)
    zookeeperDriver.createNode(clusterVersionPath, "1", CreateMode.PERSISTENT)

    val voters: util.List[String] = zookeeperDriver.getChildren(votePath)
    val currVoters: mutable.Buffer[String] =
      voters.filter(_.startsWith(s"${zookeeperDriver.getSessionId()}$voteSplit"))
    if (currVoters.size == 0) {
      voterId = zookeeperDriver.createNode(
        s"$votePath/${zookeeperDriver.getSessionId()}$voteSplit",
        voteNodeValue, CreateMode.EPHEMERAL_SEQUENTIAL)
      println(voterId)
      voterId = voterId.replace(s"$votePath/", "")
    }
    else {
      voterId = currVoters(0)
    }
    voterId
  }


  def getVoteResult(): (String, String) = {
    val mySeq = voterId.split(voteSplit)(1)
    val voters: util.List[String] = zookeeperDriver.getChildren(votePath)
    val sortedVoters = voters.sortBy(_.split(voteSplit)(1))

    if (mySeq == sortedVoters(0).split(voteSplit)(1)) {
      this.voterPlace = (MASTER, votePath)

    } else {
      val prevVoter = sortedVoters(sortedVoters.indexOf(voterId) - 1)
      this.voterPlace = (SLAVE, votePath + "/" + prevVoter)
    }
    this.voterPlace
  }

  def watchVoteNode(): util.List[String] = {
    getVoteResult()
    if (voterPlace._1.equals(MASTER)) {
      println(s"[${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}]watchVoteNode:i am  master.place:$voterPlace")
      zookeeperDriver.getChildrenAndWatch(votePath, VoteNodeWatcher)
    } else {
      println(s"[${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}]watchVoteNode:i am  salve.place:$voterPlace")
      try {
        val res = zookeeperDriver.existsAndWatch(voterPlace._2, PreNodeWatcher)
        if (res == null) {
          watchVoteNode()
          println(s"[${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}]watchVoteNode:Warining:Prev Node is not exists, will getVoteResult() and retry!")
        }
        new util.ArrayList[String]()
      }
      catch {
        case ex: KeeperException.NoNodeException => {
          Thread.sleep(100)
          watchVoteNode()
          println(s"[${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())}]watchVoteNode:Exception:Prev Node is not exists, will getVoteResult() and retry!")
          new util.ArrayList[String]()
        }
      }
    }
  }

  def watchStatusNode() = {
    zookeeperDriver.existsAndWatch(clusterStatusPath, ClusterStatusWatcher)
  }

  def acvtiveCluster() = {
    zookeeperDriver.createNode(clusterStatusPath, "", CreateMode.EPHEMERAL)
  }

  def inAcvtiveCluster() = {
    zookeeperDriver.deleteNode(clusterStatusPath)
  }

  def watchClusterVersion() = {
    zookeeperDriver.getDataAndWatch(clusterVersionPath,
      ClusterVersionWatcher)
  }

  def incrClusterVersion() = {
    var versionStr = zookeeperDriver.getData(clusterVersionPath)
    var newVersion = versionStr.toInt + 1
    zookeeperDriver.setData(clusterVersionPath, newVersion.toString())
  }

  def getClusterStatus() = {
    val stat = zookeeperDriver.exists(clusterStatusPath)
    if (stat == null) {
      "INACTIVE"
    } else {
      "ACTIVE"
    }
  }

  def isMaster(): Boolean = {
    MASTER.equals(voterPlace._1)
  }

  def breakConnectionFromZk(): Unit = {
    appManagerStandardSkill.suspend()
    zookeeperDriver.close()
  }

  def appSuspend() = {
    if (appStatus.equals("ACTIVE")) {
      appStatus = "INACTIVE"
      try {
        appManagerStandardSkill.suspend()
      } catch {
        case ex: Exception => throw new AppClusterFatalException()
      }
    }
  }

  def appResume() = {
    if (appStatus.equals("INACTIVE")) {
      appStatus = "ACTIVE"
      try {
        appManagerStandardSkill.resume()
      } catch {
        case ex: Exception => throw new AppClusterFatalException()
      }
    }
  }

  def appReconstruction() = {
    try {
      appManagerStandardSkill.reconstruction()
    } catch {
      case ex: Exception => throw new AppClusterFatalException()
    }
  }

  def appReallocation() = {
    try {
      appManagerStandardSkill.reallocation()
    } catch {
      case ex: Exception => throw new AppClusterFatalException()
    }
  }
}

object ZApplicationManager {

  private var zApplicationManager: ZApplicationManager = _

  def apply(projectName: String, zkUrls: String,
            minLiveServCount: Int, clusterInitServCount: Int,
            appManagerStandardSkill: AppManagerStandardSkill): ZApplicationManager = {
    if (zApplicationManager == null) {
      this.synchronized {
        if (zApplicationManager == null) {
          zApplicationManager = new ZApplicationManager(projectName, zkUrls, minLiveServCount, clusterInitServCount, appManagerStandardSkill)
        }
      }
    }
    zApplicationManager
  }

  def apply(): ZApplicationManager = {
    zApplicationManager
  }

  def main(args: Array[String]): Unit = {
    val zApplicationManager: ZApplicationManager = ZApplicationManager("MyProj", "192.168.186.100:2181,192.168.186.101:2181,192.168.186.102:2181",
      2, 5, new AppManagerStandardSkillImpl())

    zApplicationManager.implementZManagement()
    Thread.sleep(Long.MaxValue)
  }
}
