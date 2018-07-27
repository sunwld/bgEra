package com.collie.bgEra.hpdc.workUnit

import com.collie.bgEra.cloudApp.dtsf.{ResourceManager, WorkUnitRunable}
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo
import com.collie.bgEra.cloudApp.ssh2Pool.{Ssh2Session, SshResult}
import com.collie.bgEra.commons.util.{SerialNumberUtils, StringUtils}
import com.collie.bgEra.hpdc.service.StatisticsCalculateIncacheService
import com.collie.bgEra.hpdc.service.bean.CalculateIncacheStatsValue
import com.collie.bgEra.hpdc.workUnit.bean.{ SwapIOStats}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}

import scala.collection.JavaConversions._
import scala.collection.mutable

class SwapIOStatsCaptcher extends WorkUnitRunable {
  private val TOPIC = "hpdc-swapio"
  private val SHELL = "SWAP_PAGESTAT"


  private val logger: Logger = LoggerFactory.getLogger("hpdc")

  @Autowired
  @Qualifier("hostShellMap")
  private val shellMap: java.util.Map[String, String] = null

  @Autowired
  private val kfkProducer: KafkaProducer[String, SwapIOStats] = null

  @Autowired
  private val resManager: ResourceManager = null

  /**
    * String1: network device name
    * String2: network stats name: Array(ipks, ierrs, opks, oerrs)
    */
  @Autowired
  private val statisticsCalculateIncacheService: StatisticsCalculateIncacheService[String] = null

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

    val cmd = shellMap.get(SHELL)
    var session: Ssh2Session = null
    var sshResult: SshResult = null


    try {
      session = resManager.getHostSshConnPoolResource(workUnitInfo.getTargetId())
      sshResult = session.execCommand(cmd)
      val snapId = SerialNumberUtils.getSerialByTrunc10s(workUnitInfo.thisTime, true)

      if (sshResult.isFinishAndCmdSuccess()) {
        val swapInRegx = "^(\\d+)\\s+.*\\s+in.*$".r
        val swapOutRegx = "^(\\d+)\\s+.*\\s+out.*$".r

        var inV, outV = 0D

        sshResult.getStrout().foreach(line => {
          line match {
            case swapInRegx(c) => inV = StringUtils.toDouble(c)
            case swapOutRegx(c) => outV = StringUtils.toDouble(c)
            case _ => {}
          }
        })

        val calculateIncacheStatsValue: CalculateIncacheStatsValue[String] = new CalculateIncacheStatsValue(
          snapId, mutable.HashMap[String, (String, Double)](SwapIOStats.swapIn -> (snapId, inV),
            SwapIOStats.swapOut -> (snapId, outV)))

        val caledValMap = statisticsCalculateIncacheService.calculateDiff2LastValue(SHELL, workUnitInfo.targetId, calculateIncacheStatsValue)

        val swapIOStats: SwapIOStats = new SwapIOStats()
        swapIOStats.snapId = snapId
        swapIOStats.targetId = workUnitInfo.targetId
        swapIOStats.statsResult = (workUnitInfo.targetId, snapId, caledValMap(SwapIOStats.swapIn)._3, caledValMap(SwapIOStats.swapOut)._3)

        val record: ProducerRecord[String, SwapIOStats] = new ProducerRecord(TOPIC, workUnitInfo.targetId, swapIOStats)
        kfkProducer.send(record)
      }
    } catch {
      case e: Exception => {
        logger.warn(s"Captcher NetworkStats failed, shell result[${sshResult}]", e)
      }
    } finally {
      if (session != null) {
        session.close()
      }
    }

  }
}
