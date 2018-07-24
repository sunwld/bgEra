package com.collie.bgEra.hpdc.workUnit

import java.util
import java.util.Properties

import com.collie.bgEra.cloudApp.dtsf.{ResourceManager, WorkUnitRunable}
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo
import com.collie.bgEra.cloudApp.ssh2Pool.{Ssh2Session, SshResult}
import com.collie.bgEra.commons.util.SerialNumberUtils
import com.collie.bgEra.hpdc.workUnit.bean.CpuStats
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Component

import scala.util.control.Breaks._
import scala.collection.JavaConversions._
import scala.util.control.Breaks.breakable

@Component("cpuStatsCaptcher")
class CpuStatsCaptcher extends WorkUnitRunable{
  private val TOPIC = "hpdc-cpusum"
  private val SHELL = "CPU"
  private val logger: Logger = LoggerFactory.getLogger("hpdc")

  @Autowired
  @Qualifier("hostShellMap")
  private val shellMap: java.util.Map[String,String] = null

  @Autowired
  private val kfkProducer: KafkaProducer[String, Object] = null

  @Autowired
  private val resManager: ResourceManager = null

  override def runWork(unit: WorkUnitInfo): Unit = {
    val cmd = shellMap.get("CPU.xsh")
    var session: Ssh2Session = null
    var sshResult: SshResult = null
    try {
      session = resManager.getHostSshConnPoolResource(unit.getTargetId())
      sshResult = session.execCommand(cmd)
      if(sshResult.isFinishAndCmdSuccess()){
        val cpu = new CpuStats()
        val record: ProducerRecord[String,Object] = new ProducerRecord(TOPIC,unit.targetId,cpu)
        cpu.targetId = unit.targetId
        cpu.snapId = SerialNumberUtils.getSerialByTrunc10s(unit.thisTime,true)

        /**
          * USRP=0.02
          * SYSP=0.04
          * WAITP=0.00
          * IDLEP=99.94
          */
        val lines: util.List[String] = sshResult.getStrout()
        lines.foreach(line => {
          breakable {
            val lineItems = line.split("=")
            if(lineItems.length < 2){
              break
            }
            lineItems(0) match {
              case "USRP" => cpu.user = lineItems(1).toFloat
              case "SYSP" => cpu.sys = lineItems(1).toFloat
              case "WAITP" => cpu.cpuWait = lineItems(1).toFloat
              case "IDLEP" => cpu.idle = lineItems(1).toFloat
            }
          }
        })
        kfkProducer.send(record)
      }
    } catch {
      case e:Exception => {
        logger.warn(s"Captcher cpusum failed, shell result[${sshResult}]",e)
      }
    }finally{
      if(session != null){
        session.close()
      }
    }

  }
}
