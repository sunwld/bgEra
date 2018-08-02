package com.collie.bgEra.hpdc.workUnit

import java.util

import com.collie.bgEra.cloudApp.dtsf.{ResourceManager, WorkUnitRunable}
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo
import com.collie.bgEra.cloudApp.ssh2Pool.{Ssh2Session, SshResult}
import com.collie.bgEra.commons.util.{SerialNumberUtils, StringUtils}
import com.collie.bgEra.hpdc.context.KafkaProducerSource
import com.collie.bgEra.hpdc.service.ShhShellMessgesService
import com.collie.bgEra.hpdc.service.bean.ShellInfo
import com.collie.bgEra.hpdc.workUnit.bean.{CpuProcessorStats, HostIOStats}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.util.control.Breaks._
import scala.collection.mutable.ListBuffer

@Component("hostIoStatsCaptcher")
class HostIoStatsCaptcher extends WorkUnitRunable {
  private val TOPIC = "hpdc-iostat"
  private val SHELL = "IOSTAT.xsh"

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
        * K: Device name
        * V:(DeviceName,IOKB,avgWait,busy%,snapCount)
        *
        */
      val hostIoStats: HostIOStats = new HostIOStats()
      hostIoStats.snapId = SerialNumberUtils.getSerialByTrunc10s(workUnitInfo.thisTime, true)
      hostIoStats.targetId = workUnitInfo.targetId

      val osType = sshResult.get(0).split(":")(1)
      var formatedData: mutable.Buffer[mutable.Map[String, String]] = null
      val footRegex = "^[Aa]verage:?\\s+.*$".r

      osType match {
        case "LINUX" => {
          /**
            * LINUX
            * OSTYPE:LINUX
            * Linux 3.10.0-693.el7.x86_64 (devops1)   07/24/2018      _x86_64_        (16 CPU)
            *
            * Device:         rrqm/s   wrqm/s     r/s     w/s    rkB/s    wkB/s avgrq-sz avgqu-sz   await r_await w_await  svctm  %util
            * dm-0              0.00     0.00    0.01    0.73     1.80     5.39    19.39     0.00    2.77    4.71    2.75   1.29   0.10
            * dm-3              0.00     0.00    0.00   12.00     0.00    48.00     8.00     0.01    0.92    0.00    0.92   0.92   1.10
            *
            * Device:         rrqm/s   wrqm/s     r/s     w/s    rkB/s    wkB/s avgrq-sz avgqu-sz   await r_await w_await  svctm  %util
            * dm-2              0.00     0.00    0.00    2.00     0.00     4.00     4.00     0.00    0.00    0.00    0.00   0.00   0.00
            * dm-3              0.00     0.00    0.00    2.00     0.00     8.00     8.00     0.00    1.50    0.00    1.50   1.50   0.30
            *
            */
          val splitRegex = "\\s+".r
          val dataLineRegex = "^.*\\s+\\d+\\.?\\d*\\s+.*$".r
          val titleLineRegex = "^Device:\\s+.*$".r
          val titleListRegex = ListBuffer(("devName", "^Device:$".r), ("readKb", "^rkB/s$".r), ("writeKb", "^wkB/s$".r), ("busy", "^%util$".r), ("await", "^await$".r))
          formatedData = shhShellMessgesService.formatColumedMessages2Map(sshResult, titleLineRegex, footRegex, dataLineRegex, splitRegex, titleListRegex)
        }
        case "AIX" => {
          /*
            * AIX:
            * OSTYPE:AIX
            *
            * AIX bildb1 1 6 00F69A714C00    07/24/18
            *
            * System configuration: lcpu=64 drives=1246  mode=Capped
            *
            * 11:35:09     device    %busy    avque    r+w/s    Kbs/s   avwait   avserv
            *
            * 11:35:10     hdisk3      0      0.0        0        0      0.0      0.0
            *              hdisk1     72      0.0      173      692      0.0      4.6
            *              hdisk14      1      0.0        2      128      0.0      0.7
            * *** ***
            *              hdisk1070      0      0.0        0        0      0.0      0.0
            *              cd1      0      0.0        0        0      0.0      0.0
            *              cd0      0      0.0        0        0      0.0      0.0
            *
            * 11:35:11     hdisk3      0      0.0        0        0      0.0      0.0
            *              hdisk1     71      0.0      169      677      0.0      4.5
            *              cd0      0      0.0        0        0      0.0      0.0
            *
            * Average      hdisk3      0      0.0        0        0      0.0      0.0
            *              hdisk1      1      0.0        2        8      0.0      3.8
            *              hdisk0      2      0.0        2        8      0.0      3.9
            */
          val splitRegex = "\\s+".r
          val dataLineRegex = "^(?:\\d+|\\s+).*\\s+\\d+\\.?\\d*\\s+.*$".r
          val titleLineRegex = "^.*\\s+device\\s+.*$".r
          val titleListRegex = ListBuffer(("devName", "^device$".r), ("totalKb", "^Kbs/s$".r), ("await", "^avserv$".r), ("busy", "^%busy$".r))
          formatedData = shhShellMessgesService.formatColumedMessages2Map(sshResult, titleLineRegex, footRegex, dataLineRegex, splitRegex, titleListRegex)
        }
        case "SOLARIS" => {
          /*
            * SOLARIS:
            * OSTYPE:SOLARIS
            *
            * SunOS cspdb1 5.11 11.1 sun4v    07/24/2018
            *
            * 11:42:23   device        %busy   avque   r+w/s  blks/s  avwait  avserv
            *
            *
            * 11:42:24   fp0               0     0.0       0       0     0.0     0.0
            *            fp2              99     2.8    1050  117269     0.0     2.6
            *            sd5              13     0.1     103    7737     0.0     1.4
            *            sd5,a             0     0.0       0       0     0.0     0.0
            *            sd5,h             0     0.0       1      16     0.0     0.4
            *            sd7              33     0.5     169   15364     0.0     2.8
            * *** ***
            * 11:42:25   fp0               0     0.0       0       0     0.0     0.0
            *            fp2              98     2.2    1132   32001     0.0     2.0
            * Average    fp0               0     0.0       0       0     0.0     0.0
            *            fp2              99     2.5    1091   74918     0.0     2.3
            *            vdc1              0     0.0       0       0     0.0     0.0
            *
            *
            */
          val splitRegex = "\\s+".r
          val dataLineRegex = "^(?:\\d+|\\s+).*\\s+\\d+\\.?\\d*\\s+.*$".r
          val titleLineRegex = "^.*\\s+device\\s+.*$".r
          val titleListRegex = ListBuffer(("devName", "^device$".r), ("totalKb", "^Kbs/s$".r), ("await", "^avserv$".r), ("busy", "^%busy$".r))
          formatedData = shhShellMessgesService.formatColumedMessages2Map(sshResult, titleLineRegex, footRegex, dataLineRegex, splitRegex, titleListRegex)

        }
        case _ => {}
      }

      var devName: String = null
      var readKb, writeKb, totalKb, await: Double = 0D
      var busy: Float = 0F

      val diskStatList = ListBuffer[(String, Double, Double, Float, Int)]()
      formatedData.foreach(fd => {
        devName = fd("devName")
        if ("LINUX".equals(osType)) {
          readKb = StringUtils.toDouble(fd.getOrElse("readKb", null))
          writeKb = StringUtils.toDouble(fd.getOrElse("writeKb", null))
          totalKb = 0
        } else {
          readKb = 0
          writeKb = 0
          totalKb = StringUtils.toDouble(fd.getOrElse("totalKb", null))
        }
        await = StringUtils.toDouble(fd.getOrElse("await", null))
        busy = StringUtils.toFloat(fd.getOrElse("busy", null))
        diskStatList.append((devName, readKb + writeKb + totalKb, await, busy, 1))
      })

      val tempSR = mutable.Map(diskStatList.groupBy(_._1).mapValues(vs => {
        vs.reduce((sv, iv) => {
          (iv._1, sv._2 + iv._2, sv._3 + iv._3, sv._4 + iv._4, sv._5 + iv._5)
        })
      }).toSeq: _*)

      hostIoStats.statsResult = new java.util.HashMap()
      tempSR.foreach(v => {
        hostIoStats.statsResult.put(v._1, (v._2._2, v._2._3, v._2._4, v._2._5))
      })

      logger.debug(s"hostIoStatsCaptcher:${hostIoStats.toString()}")

      val producerRecord = new ProducerRecord(TOPIC, hostIoStats.targetId, hostIoStats)
      shhShellMessgesService.sendRecord2Kafka(producerRecord, classOf[HostIOStats])
    } catch {
      case e: Exception => {
        logger.warn(s"Captcher cpusum failed.", e)
      }
    }
  }

}
