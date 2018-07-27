package com.collie.bgEra.hpdc.workUnit

import java.util

import com.collie.bgEra.cloudApp.dtsf.{ResourceManager, WorkUnitRunable}
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo
import com.collie.bgEra.cloudApp.ssh2Pool.{Ssh2Session, SshResult}
import com.collie.bgEra.commons.util.{ArrayUtils, SerialNumberUtils, StringUtils}
import com.collie.bgEra.hpdc.service.StatisticsCalculateIncacheService
import com.collie.bgEra.hpdc.service.bean.CalculateIncacheStatsValue
import com.collie.bgEra.hpdc.workUnit.bean.{HostNetStats, MemStats}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._
import scala.collection.mutable

@Component("networkStatsCaptcher")
class NetworkStatsCaptcher extends WorkUnitRunable {
  private val TOPIC = "hpdc-cpusum"
  private val SHELL = "NET"
  private val logger: Logger = LoggerFactory.getLogger("hpdc")

  @Autowired
  @Qualifier("hostShellMap")
  private val shellMap: java.util.Map[String, String] = null

  @Autowired
  private val kfkProducer: KafkaProducer[String, HostNetStats] = null

  @Autowired
  private val resManager: ResourceManager = null

  /**
    * String1: network device name
    * String2: network stats name: Array(ipks, ierrs, opks, oerrs)
    */
  @Autowired
  private val statisticsCalculateIncacheService: StatisticsCalculateIncacheService[(String, String)] = null

  override def runWork(workUnitInfo: WorkUnitInfo): Unit = {

    val cmd = shellMap.get(SHELL)
    var session: Ssh2Session = null
    var sshResult: SshResult = null


    try {
      session = resManager.getHostSshConnPoolResource(workUnitInfo.getTargetId())
      sshResult = session.execCommand(cmd)

      if (sshResult.isFinishAndCmdSuccess()) {
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

        val hostNetStats: HostNetStats = new HostNetStats()
        hostNetStats.targetId = workUnitInfo.targetId
        val snapId = SerialNumberUtils.getSerialByTrunc10s(workUnitInfo.thisTime, true)
        hostNetStats.snapId = snapId

        val titleRegx = "([Mm][Tt][Uu]\\s+\\w*\\s*[Rr][Xx]-[Oo][Kk]\\s+)|([Mm][Tt][Uu]\\s+\\w*\\s*[Ii][Pp][Kk][Ss]\\s+)".r
        val dataRegx = "\\s+\\d+\\s+".r
        val dataSplit = "\\s+".r

        var nameP, mtuP, ipkP, ierrP, opkP, oerrP = -1

        val valMap = mutable.Map[(String, String), (String, Double)]()
        //name -> mtu
        val ifDevMap = mutable.HashMap[String, Int]()
        val calculateIncacheStatsValue: CalculateIncacheStatsValue[(String, String)] = new CalculateIncacheStatsValue(snapId, valMap)

        //statid:ipks, ierrs, opks, oerrs, mtu
        sshResult.getStrout.foreach(line => {


          if (!dataRegx.findFirstIn(line).isEmpty) {
            // lo0   8232 127.0.0.0     127.0.0.1      1141313777 0     1141313777 0     0      0
            val dataItems = dataSplit.split(line)
            val name = ArrayUtils.geti(dataItems, nameP)
            val mtu = StringUtils.toInt(ArrayUtils.geti(dataItems, mtuP))
            val ipk = StringUtils.toDouble(ArrayUtils.geti(dataItems, ipkP))
            val ierr = StringUtils.toDouble(ArrayUtils.geti(dataItems, ierrP))
            val opk = StringUtils.toDouble(ArrayUtils.geti(dataItems, opkP))
            val oerr = StringUtils.toDouble(ArrayUtils.geti(dataItems, oerrP))

            valMap.put((name, HostNetStats.ipks), (snapId, ipk))
            valMap.put((name, HostNetStats.ierrs), (snapId, ierr))
            valMap.put((name, HostNetStats.opks), (snapId, opk))
            valMap.put((name, HostNetStats.oerrs), (snapId, oerr))
            ifDevMap.put(name, mtu)

          } else if (!titleRegx.findFirstIn(line).isEmpty) {
            // Name  Mtu  Net/Dest      Address        Ipkts  Ierrs Opkts  Oerrs Collis Queue
            val titleItems = dataSplit.split(line)
            for (i <- 0 until titleItems.length) {
              titleItems(i) match {
                case tt if ("Mtu".equals(tt) || "MTU".equals(tt) || "mtu".equals(tt)) => mtuP = i
                case tt if ("Name".equals(tt) || "Iface".equals(tt) || "name".equals(tt)) => nameP = i
                case tt if ("Ipkts".equals(tt) || "RX-OK".equals(tt) || tt.contains("RX") || tt.contains("rx") || tt.contains("ipk")) => ipkP = i
                case tt if ("RX-ERR".equals(tt) || "Ierrs".equals(tt) || tt.contains("ierr") || tt.contains("rx-err")) => ierrP = i
                case tt if ("TX-OK".equals(tt) || "Opkts".equals(tt) || tt.contains("TX") || tt.contains("tx") || tt.contains("opk")) => opkP = i
                case tt if ("TX-ERR".equals(tt) || "Oerrs".equals(tt) || tt.contains("oerr") || tt.contains("tx-err")) => oerrP = i
                case _ => {}
              }
            }
          }
        })
        val caledValMap: mutable.Map[(String, String), (String, Long, Double)] = statisticsCalculateIncacheService.calculateDiff2LastValue(SHELL, hostNetStats.targetId, calculateIncacheStatsValue)
        //(name,snapid,mtu,ipks,ierrs,opks,oerrs)
        val ifDevStatVal: mutable.Map[String, (String, String, Int, Long, Long, Long, Long)] = mutable.HashMap()
        caledValMap.groupBy(_._1._1).foreach(caledIfDev => {
          val ifDevStatMap = caledIfDev._2
          val devname = caledIfDev._1
          val mtu = ifDevMap(devname)
          val ipks = ifDevStatMap((devname, HostNetStats.ipks))._3.toLong
          val ierrs = ifDevStatMap((devname, HostNetStats.ierrs))._3.toLong
          val opks = ifDevStatMap((devname, HostNetStats.opks))._3.toLong
          val oerrs = ifDevStatMap((devname, HostNetStats.oerrs))._3.toLong
          ifDevStatVal.put(devname, (devname, snapId, mtu, ipks, ierrs, opks, oerrs))
        })

        hostNetStats.statsResult = ifDevStatVal
        val record: ProducerRecord[String, HostNetStats] = new ProducerRecord(TOPIC, workUnitInfo.targetId, hostNetStats)
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
