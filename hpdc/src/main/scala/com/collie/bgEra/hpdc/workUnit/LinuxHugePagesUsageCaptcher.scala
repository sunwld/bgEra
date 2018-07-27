package com.collie.bgEra.hpdc.workUnit

import com.collie.bgEra.cloudApp.dtsf.{ResourceManager, WorkUnitRunable}
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo
import com.collie.bgEra.cloudApp.ssh2Pool.{Ssh2Session, SshResult}
import com.collie.bgEra.commons.util.{SerialNumberUtils, StringUtils}
import com.collie.bgEra.hpdc.workUnit.bean.{HostNetStats, LinuxHugePagesUsageStats}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}

import scala.collection.JavaConversions._

class LinuxHugePagesUsageCaptcher extends WorkUnitRunable {
  private val TOPIC = "hpdc-hugepage"
  private val SHELL = "LINUX_HUGEPG"

  private val logger: Logger = LoggerFactory.getLogger("hpdc")

  @Autowired
  @Qualifier("hostShellMap")
  private val shellMap: java.util.Map[String, String] = null

  @Autowired
  private val kfkProducer: KafkaProducer[String, LinuxHugePagesUsageStats] = null

  @Autowired
  private val resManager: ResourceManager = null

  override def runWork(workUnitInfo: WorkUnitInfo): Unit = {

    val cmd = shellMap.get(SHELL)
    var session: Ssh2Session = null
    var sshResult: SshResult = null

    try {
      session = resManager.getHostSshConnPoolResource(workUnitInfo.getTargetId())
      sshResult = session.execCommand(cmd)

      /**
        * AnonHugePages:   2500608 kB
        * HugePages_Total:       0
        * HugePages_Free:        0
        * HugePages_Rsvd:        0
        * HugePages_Surp:        0
        * Hugepagesize:       2048 kB
        */

      if (sshResult.isFinishAndCmdSuccess()) {
        val hugePageTotalCountRegx = "^HugePages_Total[:]?\\s+(\\d+)".r
        val hugePageFreeRegx = "^HugePages_Free[:]?\\s+(\\d+)".r
        val hugePageRsvdRegx = "^HugePages_Rsvd[:]?\\s+(\\d+)".r
        val hugePageSurpRegx = "^HugePages_Surp[:]?\\s+(\\d+)".r
        val hugepagesizeRegx = "^Hugepagesize[:]?\\s+(\\d+)".r

        var totalCount, freeCount, rsvdCount, surpCount = 0L
        var pageSize = 0

        sshResult.getStrout().foreach(line => {
          line match {
            case hugePageTotalCountRegx(c) => totalCount = StringUtils.toLong(c)
            case hugePageFreeRegx(c) => freeCount = StringUtils.toLong(c)
            case hugePageRsvdRegx(c) => rsvdCount = StringUtils.toLong(c)
            case hugePageSurpRegx(c) => surpCount = StringUtils.toLong(c)
            case hugepagesizeRegx(c) => pageSize = StringUtils.toInt(c)
            case _ => {}
          }
        })

        val linuxHugePagesUsageStats: LinuxHugePagesUsageStats = new LinuxHugePagesUsageStats()
        linuxHugePagesUsageStats.snapId = SerialNumberUtils.getSerialByTrunc10s(workUnitInfo.thisTime, true)
        linuxHugePagesUsageStats.targetId = workUnitInfo.targetId
        //targetId,snapId,HugePages_Total , HugePages_Free, HugePages_Rsvd, HugePages_Surp, Hugepagesize
        linuxHugePagesUsageStats.statsResult = (workUnitInfo.targetId, linuxHugePagesUsageStats.snapId,
          totalCount, freeCount, rsvdCount, surpCount, pageSize)

        val record: ProducerRecord[String, LinuxHugePagesUsageStats] = new ProducerRecord(TOPIC, workUnitInfo.targetId, linuxHugePagesUsageStats)
        kfkProducer.send(record)
      }
    } catch {
      case e: Exception => {
        logger.warn(s"Captcher cpusum failed, shell result[${sshResult}]", e)
      }
    } finally {
      if (session != null) {
        session.close()
      }
    }


  }
}
