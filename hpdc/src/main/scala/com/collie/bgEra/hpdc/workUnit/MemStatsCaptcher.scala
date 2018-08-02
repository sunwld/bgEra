package com.collie.bgEra.hpdc.workUnit

import java.util

import com.collie.bgEra.cloudApp.dtsf.{ResourceManager, WorkUnitRunable}
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo
import com.collie.bgEra.cloudApp.ssh2Pool.{Ssh2Session, SshResult}
import com.collie.bgEra.commons.util.{SerialNumberUtils, StringUtils}
import com.collie.bgEra.hpdc.service.ShhShellMessgesService
import com.collie.bgEra.hpdc.service.bean.ShellInfo
import com.collie.bgEra.hpdc.workUnit.bean.{CpuStats, MemStats}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.util.control.Breaks.{break, breakable}

@Component("memStatsCaptcher")
class MemStatsCaptcher extends WorkUnitRunable {
  private val TOPIC = "hpdc-memsum"
  private val SHELL = "MEM.xsh"
  private val logger: Logger = LoggerFactory.getLogger("hpdc")

  @Autowired
  private val shhShellMessgesService: ShhShellMessgesService = null

  override def runWork(unit: WorkUnitInfo): Unit = {

    try {
      val sshResult = shhShellMessgesService.loadShellResults(new ShellInfo(SHELL, unit.targetId, mutable.Map()))
      if (sshResult == null || sshResult.isEmpty) {
        return
      }

      val mem = new MemStats()
      mem.targetId = unit.targetId
      mem.snapId = SerialNumberUtils.getSerialByTrunc10s(unit.thisTime, true)

      /**
        * MEM_TOTAL=67553529856
        * MEM_FREE=10529566720
        * CACHE_INUSE=9897738240
        * SWAP_TOTAL=17179865088
        * SWAP_FREE=17179865088
        */
      var memTotal, memFree, cache, swap, swapFree: Long = 0
      sshResult.foreach(line => {
        breakable {
          val lineItems = line.split("=")
          if (lineItems.length < 2) {
            break
          }
          lineItems(0) match {
            case "MEM_TOTAL" => memTotal = StringUtils.toLong(lineItems(1))
            case "MEM_FREE" => memFree = StringUtils.toLong(lineItems(1))
            case "CACHE_INUSE" => cache = StringUtils.toLong(lineItems(1))
            case "SWAP_TOTAL" => swap = StringUtils.toLong(lineItems(1))
            case "SWAP_FREE" => swapFree = StringUtils.toLong(lineItems(1))
          }
        }
      })
      mem.statsResult = (memTotal, memFree, cache, swap, swapFree)
      logger.debug(s"memStatsCaptcher:${mem.toString()}")

      val record: ProducerRecord[String, MemStats] = new ProducerRecord(TOPIC, unit.targetId, mem)
      shhShellMessgesService.sendRecord2Kafka(record, classOf[MemStats])

    } catch {
      case e: Exception => {
        logger.warn(s"Captcher memsum failed.", e)
      }
    }
  }
}
