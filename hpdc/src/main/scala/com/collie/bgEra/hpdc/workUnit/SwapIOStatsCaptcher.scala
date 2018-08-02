package com.collie.bgEra.hpdc.workUnit

import com.collie.bgEra.cloudApp.dtsf.{ResourceManager, WorkUnitRunable}
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo
import com.collie.bgEra.cloudApp.ssh2Pool.{Ssh2Session, SshResult}
import com.collie.bgEra.commons.util.{SerialNumberUtils, StringUtils}
import com.collie.bgEra.hpdc.service.{ShhShellMessgesService, StatisticsCalculateIncacheService}
import com.collie.bgEra.hpdc.service.bean.{CalculateIncacheStatsValue, ShellInfo}
import com.collie.bgEra.hpdc.workUnit.bean.SwapIOStats
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._
import scala.collection.mutable

@Component("swapIOStatsCaptcher")
class SwapIOStatsCaptcher extends WorkUnitRunable {
  private val TOPIC = "hpdc-swapio"
  private val SHELL = "SWAP_PAGESTAT.xsh"

  private val logger: Logger = LoggerFactory.getLogger("hpdc")

  @Autowired
  private val shhShellMessgesService: ShhShellMessgesService = null

  /**
    * String1: network device name
    * String2: network stats name: Array(ipks, ierrs, opks, oerrs)
    */
  @Autowired
  private val statisticsCalculateIncacheService: StatisticsCalculateIncacheService = null

  override def runWork(workUnitInfo: WorkUnitInfo): Unit = {
    /**
      * OSTYPE:LINUX
      * 0 pages swapped in
      * 0 pages swapped out
      */

    /**
      * OSTYPE:AIX
      * 4924134 paging space page ins
      * 7024230 paging space page outs
      */

    /**
      * OSTYPE:SOLARIS
      * 0 pages swapped in
      * 0 pages swapped out
      */


    try {
      val sshResult = shhShellMessgesService.loadShellResults(new ShellInfo(SHELL, workUnitInfo.targetId, mutable.Map()))
      if (sshResult == null || sshResult.isEmpty) {
        return
      }

      val snapId = SerialNumberUtils.getSerialByTrunc10s(workUnitInfo.thisTime, true)
      val swapInRegx = "^(\\d+)\\s+.*\\s+in.*$".r
      val swapOutRegx = "^(\\d+)\\s+.*\\s+out.*$".r

      var inV, outV = 0D

      sshResult.foreach(line => {
        line match {
          case swapInRegx(c) => inV = StringUtils.toDouble(c)
          case swapOutRegx(c) => outV = StringUtils.toDouble(c)
          case _ => {}
        }
      })

      val serableJavaData = new java.util.HashMap[String, (String, Double)]()
      serableJavaData.put(SwapIOStats.swapIn, (snapId, inV))
      serableJavaData.put(SwapIOStats.swapOut, (snapId, outV))
      val calculateIncacheStatsValue: CalculateIncacheStatsValue[String] = new CalculateIncacheStatsValue(
        snapId, serableJavaData)

      val swapIOStats: SwapIOStats = new SwapIOStats()
      swapIOStats.snapId = snapId
      swapIOStats.targetId = workUnitInfo.targetId

      val caledValMap = statisticsCalculateIncacheService.calculateDiff2LastValue(SHELL, workUnitInfo.targetId, calculateIncacheStatsValue)
      if (!caledValMap.isEmpty()) {

        swapIOStats.statsResult = (caledValMap.getOrElse(SwapIOStats.swapIn, null)._3, caledValMap(SwapIOStats.swapOut)._3)

        val record: ProducerRecord[String, SwapIOStats] = new ProducerRecord(TOPIC, workUnitInfo.targetId, swapIOStats)
        shhShellMessgesService.sendRecord2Kafka(record, classOf[SwapIOStats])
      }

      logger.debug(s"swapIOStatsCaptcher:${swapIOStats.toString()}")


    } catch {
      case e: Exception => {
        logger.warn(s"Captcher NetworkStats failed.", e)
      }
    }

  }
}
