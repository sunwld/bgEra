package com.collie.bgEra.hpdc.workUnit

import java.util

import com.collie.bgEra.cloudApp.dtsf.{ResourceManager, WorkUnitRunable}
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo
import com.collie.bgEra.cloudApp.ssh2Pool.{Ssh2Session, SshResult}
import com.collie.bgEra.commons.util.SerialNumberUtils
import com.collie.bgEra.hpdc.workUnit.bean.MemStats
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Component

import scala.util.control.Breaks.{break, breakable}

@Component("networkStatsCaptcher")
class NetworkStatsCaptcher extends WorkUnitRunable{
  private val TOPIC = "hpdc-cpusum"
  private val logger: Logger = LoggerFactory.getLogger("hpdc")

  @Autowired
  @Qualifier("hostShellMap")
  private val shellMap: java.util.Map[String,String] = null

  @Autowired
  private val kfkProducer: KafkaProducer[String, Object] = null

  @Autowired
  private val resManager: ResourceManager = null

  override def runWork(unit: WorkUnitInfo): Unit = {
    val cmd = shellMap.get("NET.xsh")
    var session: Ssh2Session = null
    var sshResult: SshResult = null
    try {
      session = resManager.getHostSshConnPoolResource(unit.getTargetId())
      sshResult = session.execCommand(cmd)
      if(sshResult.isFinishAndCmdSuccess()){
        val mem = new MemStats()
        val record: ProducerRecord[String,Object] = new ProducerRecord(TOPIC,unit.targetId,mem)
        mem.targetId = unit.targetId
        mem.snapId = SerialNumberUtils.getSerialByTrunc10s(unit.thisTime,true)

        /**
          * MEM_TOTAL=67553529856
          * MEM_FREE=10529566720
          * CACHE_INUSE=9897738240
          * SWAP_TOTAL=17179865088
          * SWAP_FREE=17179865088
          */
        val lines: util.List[String] = sshResult.getStrout()
        lines.foreach(line => {
          breakable {
            val lineItems = line.split("=")
            if(lineItems.length < 2){
              break
            }
            lineItems(0) match {
              case "MEM_TOTAL" => mem.memTotal = lineItems(1).toLong
              case "MEM_FREE" => mem.memFree = lineItems(1).toLong
              case "CACHE_INUSE" => mem.cacheInuse = lineItems(1).toLong
              case "SWAP_TOTAL" => mem.swapTotal = lineItems(1).toLong
              case "SWAP_FREE" => mem.swapFree = lineItems(1).toLong
            }
          }
        })
        kfkProducer.send(record)
      }
    } catch {
      case e:Exception => {
        logger.warn(s"Captcher memsum failed, shell result[${sshResult}]",e)
      }
    }finally{
      if(session != null){
        session.close()
      }
    }
  }
  }
}
