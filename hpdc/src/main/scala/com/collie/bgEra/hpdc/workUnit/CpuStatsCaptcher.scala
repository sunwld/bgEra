package com.collie.bgEra.hpdc.workUnit

import java.util
import java.util.Properties

import com.collie.bgEra.cloudApp.dtsf.{ResourceManager, WorkUnitRunable}
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo
import com.collie.bgEra.cloudApp.ssh2Pool.{Ssh2Session, SshResult}
import com.collie.bgEra.commons.util.{SerialNumberUtils, StringUtils}
import com.collie.bgEra.hpdc.context.KafkaProducerSource
import com.collie.bgEra.hpdc.service.ShhShellMessgesService
import com.collie.bgEra.hpdc.service.bean.ShellInfo
import com.collie.bgEra.hpdc.workUnit.bean.{CpuProcessorStats, CpuStats}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Component

import scala.util.control.Breaks._
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.util.control.Breaks.breakable

@Component("cpuStatsCaptcher")
class CpuStatsCaptcher extends WorkUnitRunable {
  private val TOPIC = "hpdc-cpusum"
  private val SHELL = "CPU.xsh"
  private val logger: Logger = LoggerFactory.getLogger("hpdc")

  @Autowired
  private val shhShellMessgesService: ShhShellMessgesService = null

  override def runWork(unit: WorkUnitInfo): Unit = {

    try {
      val sshResult = shhShellMessgesService.loadShellResults(new ShellInfo(SHELL, unit.targetId, mutable.Map()))
      if (sshResult == null || sshResult.isEmpty) {
        return
      }

      val cpu = new CpuStats()
      cpu.targetId = unit.targetId
      cpu.snapId = SerialNumberUtils.getSerialByTrunc10s(unit.thisTime, true)

      /**
        * USRP=0.02
        * SYSP=0.04
        * WAITP=0.00
        * IDLEP=99.94
        */
      val lines: util.List[String] = sshResult
      var user, sys, ioWait, idle: Float = 0F
      lines.foreach(line => {
        breakable {
          val lineItems = line.split("=")
          if (lineItems.length < 2) {
            break
          }
          lineItems(0) match {
            case "USRP" => user = StringUtils.toFloat(lineItems(1))
            case "SYSP" => sys = StringUtils.toFloat(lineItems(1))
            case "WAITP" => ioWait = StringUtils.toFloat(lineItems(1))
            case "IDLEP" => idle = StringUtils.toFloat(lineItems(1))
          }
        }
      })
      cpu.statsResult = (user, sys, ioWait, idle)

      logger.debug(s"cpuStatsCaptcher:${cpu.toString()}")

      val record: ProducerRecord[String, CpuStats] = new ProducerRecord(TOPIC, unit.targetId, cpu)
      shhShellMessgesService.sendRecord2Kafka(record, classOf[CpuStats])

    } catch {
      case e: Exception => {
        logger.warn(s"Captcher cpusum failed.", e)
      }
    }

  }
}
