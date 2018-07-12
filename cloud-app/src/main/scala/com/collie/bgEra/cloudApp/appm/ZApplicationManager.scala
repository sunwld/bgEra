package com.collie.bgEra.cloudApp.appm

import java.util

import com.collie.bgEra.cloudApp.appm.watchers.{ClusterStatusWatcher, ClusterVersionWatcher, PreNodeWatcher, VoteNodeWatcher}
import com.collie.bgEra.cloudApp.base.ZookeeperSession
import com.collie.bgEra.cloudApp.utils.ContextHolder
import org.apache.zookeeper.{CreateMode, KeeperException}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._
import scala.collection.mutable


class ZApplicationManager private(
                                   val projectName: String,
                                   val minLiveServCount: Int, val clusterInitServCount: Int,
                                   val appManagerStandardSkill: AppManagerStandardSkill) {


  private val logger: Logger = LoggerFactory.getLogger("appm")
  private var zkSession: ZookeeperSession = ContextHolder.getBean("appmZkSession")
  private val MAX_RETRY_TIMES_PER_MIN: Int = 5
  private val RETRY_INTERVAL_SECONDS: Int = 1
  private var retry_times = 0
  val clusterInfo: ClusterInfo = ClusterInfo(null, null, minLiveServCount, false, projectName)

  val voteSplit = "_SEQ_"
  var projectPath = ""
  var rootPath = ""
  var votePath = ""
  var clusterStatusPath = ""
  var clusterVersionPath = ""
  var voterPlace: (String, String) = _
  val MASTER = "MASTER"
  val SLAVE = "SLAVE"

  private var appStatus = "INACTIVE"

  def recoverFromFatalError(fatalException: AppClusterFatalException, retryTimes: Int = MAX_RETRY_TIMES_PER_MIN): String = {
    appSuspend()
    try {
      disconnectFromZk()
      implementZManagement()
    } catch {
      case ex: AppClusterFatalException => {
        var retryTimesTmp = retryTimes
        if (retryTimesTmp == 0) {
          logger.error(s"recoverFromFatalError execute FAILED ! exceed max retry limit , will sleep 1 min.", ex)
          Thread.sleep(60000)
          retryTimesTmp = MAX_RETRY_TIMES_PER_MIN
        }
        logger.error(s"recoverFromFatalError execute FAILED ! sleep $RETRY_INTERVAL_SECONDS S,retry.", ex)
        Thread.sleep(RETRY_INTERVAL_SECONDS * 1000)
        recoverFromFatalError(ex, retryTimesTmp - 1)
      }
    }
  }

  def rebalanceClusterByMaster(): Unit = {
    var voterIdCount = watchVoteNode().size()
    var clusterStatus = getClusterStatus()
    val clusterInitServCount = this.clusterInitServCount
    val clusterMinServCount = this.minLiveServCount
    logger.info(s"rebalanceClusterByMaster:cluster voters count:${voterIdCount}")
    logger.info(s"rebalanceClusterByMaster:cluster zk current status:${clusterStatus}")
    logger.info(s"clusterStatus : $clusterStatus")


    if ("INACTIVE".equals(clusterStatus)) {
      //集群不可用时
      if (voterIdCount >= clusterMinServCount) {
        logger.info(s"rebalanceClusterByMaster:voterCount bigger than:${clusterMinServCount},active cluster,relocate task,incr version")
        //先做REALOOCATE任务，开始初始化集群，修改集群状态，广播事件 建立VOTERWATCHER、达到重新分配任务、开始运行任务目的
        appReallocation()

        acvtiveCluster()
        incrClusterVersion()
      }
    } else if ("ACTIVE".equals(clusterStatus)) {
      //集群可用时
      if (voterIdCount < clusterMinServCount) {
        //修改集群状态，并通过修改集群状态为INACTIVE，广播事件，达到停止业务的目的
        logger.info(s"rebalanceClusterByMaster:voterCount less than:${clusterMinServCount},inactive cluster")
        inAcvtiveCluster()
      } else {
        //先做REALOOCATE任务，修改集群版本号，通过版本号广播达到让应用重新加载任务
        logger.info(s"rebalanceClusterByMaster:voterCount bigger than:${clusterMinServCount},relocate task,incr version")
        appReallocation()
        incrClusterVersion()
      }
    }
  }

  def implementZManagement(): String = {
    if(zkSession == null) {
      logger.error(s"There is no ZookeeperDriver in spring,check your spring config.")
      throw new IllegalArgumentException("There is no ZookeeperDriver in spring,check your spring config.")
    }

    try {
      logger.info(s"implementZManagement:conn zk,create vote,watchnode,watchstatus,watchcluster")

      //
      while (!zkSession.isConnected()) {
        Thread.sleep(3000)
      }

      this.clusterInfo.currentVotid = createVoteZnode(projectName, "")
      watchVoteNode()
      watchStatusNode()
      watchClusterVersion()

      if (getClusterStatus().equals("ACTIVE")) {
        appResume()
      }

      this.clusterInfo.currentVotid
    } catch {
      case ex: AppClusterFatalException => {
        logger.error(s"ERROR!!!recoverFromFatalError:cluster reconstraction!", ex)
        recoverFromFatalError(ex)
      }
    }
  }

  def reImplementZManagement(): Unit = {
    appSuspend()
    implementZManagement()
  }

  private def createVoteZnode(projectName: String, voteNodeValue: String): String = {
    projectPath = "/" + projectName
    rootPath = s"$projectPath/appm"
    clusterStatusPath = s"$rootPath/status"
    votePath = s"$rootPath/vote"
    clusterVersionPath = s"$rootPath/clusterVersion"

    zkSession.createNode(projectPath, "", CreateMode.PERSISTENT)
    zkSession.createNode(rootPath, "", CreateMode.PERSISTENT)
    zkSession.createNode(votePath, "", CreateMode.PERSISTENT)
    zkSession.createNode(clusterVersionPath, "1", CreateMode.PERSISTENT)

    val voters: mutable.Seq[String] = zkSession.getChildren(votePath)
    val currVoters: mutable.Seq[String] =
      voters.filter(_.startsWith(s"${zkSession.getSessionId()}$voteSplit"))
    if (currVoters.size == 0) {
      this.clusterInfo.currentVotid = zkSession.createNode(
        s"$votePath/${zkSession.getSessionId()}$voteSplit",
        voteNodeValue, CreateMode.EPHEMERAL_SEQUENTIAL)
      this.clusterInfo.currentVotid = this.clusterInfo.currentVotid.replace(s"$votePath/", "")
    }
    else {
      this.clusterInfo.currentVotid = currVoters(0)
    }
    this.clusterInfo.currentVotid
  }


  def getVoteResult(): (String, String) = {
    val mySeq = this.clusterInfo.currentVotid.split(voteSplit)(1)
    val voters: mutable.Seq[String] = zkSession.getChildren(votePath)
    val sortedVoters = voters.sortBy(_.split(voteSplit)(1))

    if (mySeq == sortedVoters(0).split(voteSplit)(1)) {
      this.voterPlace = (MASTER, votePath)
      clusterInfo.isMaster = true

    } else {
      val prevVoter = sortedVoters(sortedVoters.indexOf(this.clusterInfo.currentVotid) - 1)
      this.voterPlace = (SLAVE, votePath + "/" + prevVoter)
      clusterInfo.isMaster = false
    }
    this.voterPlace
  }

  def watchVoteNode(): mutable.Seq[String] = {
    getVoteResult()
    if (voterPlace._1.equals(MASTER)) {
      logger.info(s"watchVoteNode:i am  master.place:$voterPlace")
      this.clusterInfo.clusterVotids = zkSession.getChildrenAndWatch(votePath, VoteNodeWatcher)
      this.clusterInfo.clusterVotids
    } else {
      logger.info(s"watchVoteNode:i am  salve.place:$voterPlace")
      try {
        val res = zkSession.existsAndWatch(voterPlace._2, PreNodeWatcher)
        if (res == null) {
          watchVoteNode()
          logger.info(s"watchVoteNode:Warining:Prev Node is not exists, will getVoteResult() and retry!")
        }
        this.clusterInfo.clusterVotids = mutable.Seq[String]()
        this.clusterInfo.clusterVotids
      }
      catch {
        case ex: KeeperException.NoNodeException => {
          Thread.sleep(100)
          watchVoteNode()
          logger.warn(s"watchVoteNode:Exception:Prev Node is not exists, will getVoteResult() and retry!")
          this.clusterInfo.clusterVotids = mutable.Seq[String]()
          this.clusterInfo.clusterVotids
        }
      }
    }
  }

  def watchStatusNode() = {
    zkSession.existsAndWatch(clusterStatusPath, ClusterStatusWatcher)
  }

  def acvtiveCluster() = {
    zkSession.createNode(clusterStatusPath, "", CreateMode.EPHEMERAL)
  }

  def inAcvtiveCluster() = {
    zkSession.deleteNode(clusterStatusPath)
  }

  def watchClusterVersion() = {
    zkSession.getDataAndWatch(clusterVersionPath,
      ClusterVersionWatcher)
  }

  def incrClusterVersion() = {
    var versionStr = zkSession.getData(clusterVersionPath)
    var newVersion = versionStr.toInt + 1
    zkSession.setData(clusterVersionPath, newVersion.toString())
  }

  def getClusterStatus() = {
    val stat = zkSession.exists(clusterStatusPath)
    if (stat == null) {
      "INACTIVE"
    } else {
      "ACTIVE"
    }
  }

  def isMaster(): Boolean = {
    MASTER.equals(voterPlace._1)
  }

  def appSuspend() = {
    if (appStatus.equals("ACTIVE")) {
      appStatus = "INACTIVE"
      try {
        appManagerStandardSkill.suspend(clusterInfo)
      } catch {
        case ex: Exception => throw new AppClusterFatalException("")
      }
    }
  }

  def appResume() = {
    if (appStatus.equals("INACTIVE")) {
      appStatus = "ACTIVE"
      try {
        appManagerStandardSkill.resume(clusterInfo)
      } catch {
        case ex: Exception => throw new AppClusterFatalException("")
      }
    }
  }

  def appReconstruction() = {
    try {
      appManagerStandardSkill.reconstruction(clusterInfo)
    } catch {
      case ex: Exception => throw new AppClusterFatalException("")
    }
  }

  def appReallocation() = {
    try {
      appManagerStandardSkill.reallocation(clusterInfo)
    } catch {
      case ex: Exception => throw new AppClusterFatalException("",ex)
    }
  }

  def disconnectFromZk(): Unit ={
    zkSession.close()
  }
}

object ZApplicationManager {

  private var zApplicationManager: ZApplicationManager = _

  def apply(projectName: String,
            minLiveServCount: Int, clusterInitServCount: Int,
            appManagerStandardSkill: AppManagerStandardSkill): ZApplicationManager = {
    if (zApplicationManager == null) {
      this.synchronized {
        if (zApplicationManager == null) {
          zApplicationManager = new ZApplicationManager(projectName, minLiveServCount, clusterInitServCount, appManagerStandardSkill)
        }
      }
    }
    zApplicationManager
  }

  def apply(): ZApplicationManager = {
    zApplicationManager
  }

}
