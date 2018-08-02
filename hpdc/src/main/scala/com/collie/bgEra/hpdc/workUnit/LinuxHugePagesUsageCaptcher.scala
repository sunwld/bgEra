package com.collie.bgEra.hpdc.workUnit

import com.collie.bgEra.cloudApp.dtsf.{ResourceManager, WorkUnitRunable}
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo
import com.collie.bgEra.cloudApp.ssh2Pool.{Ssh2Session, SshResult}
import com.collie.bgEra.commons.util.{SerialNumberUtils, StringUtils}
import com.collie.bgEra.hpdc.service.ShhShellMessgesService
import com.collie.bgEra.hpdc.service.bean.ShellInfo
import com.collie.bgEra.hpdc.workUnit.bean.{HostNetStats, LinuxHugePagesUsageStats}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._
import scala.collection.mutable

@Component("linuxHugePagesUsageCaptcher")
class LinuxHugePagesUsageCaptcher extends WorkUnitRunable {
  private val TOPIC = "hpdc-hugepage"
  private val SHELL = "LINUX_HUGEPG.xsh"

  private val logger: Logger = LoggerFactory.getLogger("hpdc")

  @Autowired
  private val shhShellMessgesService: ShhShellMessgesService = null

  override def runWork(workUnitInfo: WorkUnitInfo): Unit = {

    try {
      val sshResult = shhShellMessgesService.loadShellResults(new ShellInfo(SHELL, workUnitInfo.targetId, mutable.Map()))
      if (sshResult == null || sshResult.isEmpty) {
        return
      }

      /**
        * AnonHugePages:   2500608 kB
        * HugePages_Total:       0
        * HugePages_Free:        0
        * HugePages_Rsvd:        0
        * HugePages_Surp:        0
        * Hugepagesize:       2048 kB
        */
      val hugePageTotalCountRegx = "^HugePages_Total[:]?\\s+(\\d+)".r
      val hugePageFreeRegx = "^HugePages_Free[:]?\\s+(\\d+)".r
      val hugePageRsvdRegx = "^HugePages_Rsvd[:]?\\s+(\\d+)".r
      val hugePageSurpRegx = "^HugePages_Surp[:]?\\s+(\\d+)".r
      val hugepagesizeRegx = "^Hugepagesize[:]?\\s+(\\d+)".r

      var totalCount, freeCount, rsvdCount, surpCount = 0L
      var pageSize = 0

      sshResult.foreach(line => {
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
      linuxHugePagesUsageStats.statsResult = (totalCount, freeCount, rsvdCount, surpCount, pageSize)

      logger.debug(s"linuxHugePagesUsageCaptcher:${linuxHugePagesUsageStats.toString()}")

      val record: ProducerRecord[String, LinuxHugePagesUsageStats] = new ProducerRecord(TOPIC, workUnitInfo.targetId, linuxHugePagesUsageStats)
      shhShellMessgesService.sendRecord2Kafka(record, classOf[LinuxHugePagesUsageStats])

    } catch {
      case e: Exception => {
        logger.warn(s"Captcher cpusum failed.", e)
      }
    }
  }
}
