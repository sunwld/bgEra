package com.collie.bgEra.hpdc.workUnit

import java.util

import com.collie.bgEra.cloudApp.dtsf.{ResourceManager, WorkUnitRunable}
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo
import com.collie.bgEra.cloudApp.ssh2Pool.{Ssh2Session, SshResult}
import com.collie.bgEra.commons.util.{ArrayUtils, SerialNumberUtils, StringUtils}
import com.collie.bgEra.hpdc.service.{ShhShellMessgesService, StatisticsCalculateIncacheService}
import com.collie.bgEra.hpdc.service.bean.{CalculateIncacheStatsValue, ShellInfo}
import com.collie.bgEra.hpdc.workUnit.bean.{CpuProcessorStats, HostNetStats, MemStats}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

@Component("networkStatsCaptcher")
class NetworkStatsCaptcher extends WorkUnitRunable {
  private val TOPIC = "hpdc-netstat"
  private val SHELL = "NET.xsh"
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
    try {
      val sshResult = shhShellMessgesService.loadShellResults(new ShellInfo(SHELL, workUnitInfo.targetId, mutable.Map()))
      if (sshResult == null || sshResult.isEmpty) {
        return
      }

      val hostNetStats: HostNetStats = new HostNetStats()
      hostNetStats.targetId = workUnitInfo.targetId
      val snapId = SerialNumberUtils.getSerialByTrunc10s(workUnitInfo.thisTime, true)
      hostNetStats.snapId = snapId

      val titleRegx = "^.*(?:MTU\\s+.*RX-OK|Mtu\\s+.*Ipkts).*$".r
      val dataRegx = "^.*\\s+\\d+\\s+.*$".r
      val dataSplit = "\\s+".r
      val footRegx = "^Foot$".r
      val titleListRegx = ListBuffer((HostNetStats.name, "^(?:Iface|Name)$".r), (HostNetStats.mtu, "^(?:MTU|Mtu)$".r)
        , (HostNetStats.ipks, "^(?:RX-OK|Ipkts)$".r), (HostNetStats.ierrs, "^(?:RX-ERR|Ierrs)$".r),
        (HostNetStats.opks, "^(?:TX-OK|Opkts)$".r), (HostNetStats.oerrs, "^(?:TX-ERR|Oerrs)$".r))

      /**
        * LINUX:
        * OSTYPE:LINUX
        * Kernel Interface table
        * Iface      MTU    RX-OK RX-ERR RX-DRP RX-OVR    TX-OK TX-ERR TX-DRP TX-OVR Flg
        * eth0      1500 324014117      0      0 0      296716966      0      0      0 BMRU
        * lo       65536 67936113      0      0 0      67936113      0      0      0 LRU
        * virbr0    1500        0      0      0 0             0      0      0      0 BMU
        *
        * AIX:
        * OSTYPE:AIX
        * Name  Mtu   Network     Address            Ipkts Ierrs    Opkts Oerrs  Coll
        * en4   1500  link#2      e4.1f.13.50.8c.a  2619372     0  1890990     3     0
        * en4   1500  192.168.253 192.168.253.10    2619372     0  1890990     3     0
        * lo0   16896 ::1%1                        4087428230     0 4088067486     0     0
        *
        * SOLARIS:
        * OSTYPE:SOLARIS
        * Name  Mtu  Net/Dest      Address        Ipkts  Ierrs Opkts  Oerrs Collis Queue
        * lo0   8232 127.0.0.0     127.0.0.1      1141313777 0     1141313777 0     0      0
        * net3  1500 0.0.0.0       0.0.0.0        1092897 0     40386498882 0     0      0
        *
        * Name  Mtu  Net/Dest                    Address                     Ipkts  Ierrs Opkts  Oerrs Collis
        * lo0   8252 ::1                         ::1                         1141313777 0     1141313777 0     0
        * net3  1500 default                     ::                          1092897 0     40386498882 0     0
        */

      var formatedData: mutable.Buffer[mutable.Map[String, String]] = shhShellMessgesService.formatColumedMessages2Map(sshResult, titleRegx, footRegx, dataRegx, dataSplit, titleListRegx)
      var name: String = null
      var mtu: Int = 0
      var ipk, ierr, opk, oerr: Double = 0D
      val calValMap = new java.util.HashMap[(String, String), (String, Double)]()
      val otherInfoMap = mutable.Map[String, Int]()

      formatedData.foreach(fd => {
        name = fd(HostNetStats.name)
        calValMap.put((name, HostNetStats.ipks), (snapId, StringUtils.toDouble(fd.getOrElse(HostNetStats.ipks, null))))
        calValMap.put((name, HostNetStats.ierrs), (snapId, StringUtils.toDouble(fd.getOrElse(HostNetStats.ierrs, null))))
        calValMap.put((name, HostNetStats.opks), (snapId, StringUtils.toDouble(fd.getOrElse(HostNetStats.opks, null))))
        calValMap.put((name, HostNetStats.oerrs), (snapId, StringUtils.toDouble(fd.getOrElse(HostNetStats.oerrs, null))))
        otherInfoMap.put(name, StringUtils.toInt(fd.getOrElse(HostNetStats.mtu, null)))
      })

      val calculateIncacheStatsValue: CalculateIncacheStatsValue[(String, String)] = new CalculateIncacheStatsValue(snapId, calValMap)
      val caledValMap: java.util.Map[(String, String), (String, Long, Double)] = statisticsCalculateIncacheService.calculateDiff2LastValue(SHELL, hostNetStats.targetId, calculateIncacheStatsValue)

      //(name,snapid,mtu,ipks,ierrs,opks,oerrs)
      val ifDevStatVal: java.util.Map[String, (String, Int, Long, Long, Long, Long)] = new java.util.HashMap()
      caledValMap.groupBy(_._1._1).foreach(caledIfDev => {
        val ifDevStatMap = caledIfDev._2
        val devname = caledIfDev._1
        val mtu = otherInfoMap(devname)
        val ipks = ifDevStatMap((devname, HostNetStats.ipks))._3.toLong
        val ierrs = ifDevStatMap((devname, HostNetStats.ierrs))._3.toLong
        val opks = ifDevStatMap((devname, HostNetStats.opks))._3.toLong
        val oerrs = ifDevStatMap((devname, HostNetStats.oerrs))._3.toLong
        ifDevStatVal.put(devname, (snapId, mtu, ipks, ierrs, opks, oerrs))
      })

      if (!ifDevStatVal.isEmpty()) {
        hostNetStats.statsResult = ifDevStatVal
        val record: ProducerRecord[String, HostNetStats] = new ProducerRecord(TOPIC, workUnitInfo.targetId, hostNetStats)
        shhShellMessgesService.sendRecord2Kafka(record, classOf[HostNetStats])
      }

      logger.debug(s"networkStatsCaptcher:${hostNetStats.toString()}")
    } catch {
      case e: Exception => {
        logger.warn(s"Captcher NetworkStats failed.", e)
      }
    }

  }
}
