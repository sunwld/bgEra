package com.collie.bgEra.hpdc.workUnit

import java.util

import com.collie.bgEra.cloudApp.dtsf.{ResourceManager, WorkUnitRunable}
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo
import com.collie.bgEra.cloudApp.ssh2Pool.{Ssh2Session, SshResult}
import com.collie.bgEra.commons.util.{SerialNumberUtils, StringUtils}
import com.collie.bgEra.hpdc.context.KafkaProducerSource
import com.collie.bgEra.hpdc.service.ShhShellMessgesService
import com.collie.bgEra.hpdc.service.bean.ShellInfo
import com.collie.bgEra.hpdc.workUnit.bean.CpuProcessorStats
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

@Component("cpuProcessorStatsCaptcher")
class CpuProcessorStatsCaptcher extends WorkUnitRunable {
  private val TOPIC = "hpdc-cpu-processor"
  private val SHELL = "CPU_DETAIL.xsh"
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
        * CPU PROCESSOR TUPLE (cpuid,sysp,userp,iowaitp,idlep, 1)
        *
        * cpuid: cpuid
        * sysp: sys%
        * userp: user%
        * iowaitp: wait%
        * idlep: idle%
        * snapCount: default 1
        */


      var statsResult: CpuProcessorStats = new CpuProcessorStats()
      statsResult.snapId = SerialNumberUtils.getSerialByTrunc10s(workUnitInfo.thisTime, true)
      statsResult.targetId = workUnitInfo.targetId

      val osType = sshResult.get(0).split(":")(1)
      var formatedData: mutable.Buffer[mutable.Map[String, Object]] = null
      val footRegex = "^[Aa]verage:?\\s+.*$".r
      osType match {
        case "LINUX" => {
          /**
            * OSTYPE:LINUX
            * Linux 3.10.0-693.el7.x86_64 (devops1)   07/24/2018      _x86_64_        (16 CPU)
            *
            * 05:34:50 PM     CPU     %user     %nice   %system   %iowait    %steal     %idle
            * 05:34:51 PM     all      0.06      0.00      0.06      0.00      0.00     99.87
            *
            * 05:34:51 PM      14      0.99      0.00      0.00      0.00      0.00     99.01
            * 05:34:51 PM      15      0.00      0.00      0.00      0.00      0.00    100.00
            *
            * 05:34:51 PM     CPU     %user     %nice   %system   %iowait    %steal     %idle
            * 05:34:52 PM     all      0.13      0.00      0.13      0.00      0.00     99.75
            * 05:34:52 PM       0      0.00      0.00      0.00      0.00      0.00    100.00
            *
            * Average:         13      0.20      0.00      0.20      0.00      0.00     99.60
            * Average:         14      0.20      0.00      0.00      0.00      0.00     99.80
            * Average:         15      0.20      0.00      0.00      0.00      0.00     99.80
            */
          val titlePattern = "^\\d{1,2}:\\d{1,2}:\\d{1,2} [AP]M\\s+CPU\\s+.*idle$".r
          val dataPattern = "^\\d{1,2}:\\d{1,2}:\\d{1,2} [AP]M\\s+\\d+\\s+.*$".r
          val splitPattern = "\\s+".r
          val titleListPattern = ListBuffer((CpuProcessorStats.cpuId._1, "^CPU$".r, CpuProcessorStats.cpuId._2),
            (CpuProcessorStats.sys._1, "^%system$".r, CpuProcessorStats.sys._2),
            (CpuProcessorStats.ioWait._1, "^%iowait$".r, CpuProcessorStats.ioWait._2),
            (CpuProcessorStats.idle._1, "^%idle$".r, CpuProcessorStats.idle._2),
            (CpuProcessorStats.user._1, "^%user$".r, CpuProcessorStats.user._2))

          formatedData = shhShellMessgesService.formatConvertColumedMessages2Map(sshResult, titlePattern, footRegex, dataPattern, splitPattern, titleListPattern)
        }
        case "AIX" => {
          /*
            * AIX bildb1 1 6 00F69A714C00    07/24/18
            *
            * System configuration: lcpu=64  mode=Capped
            *
            * 15:53:50 cpu    %usr    %sys    %wio   %idle   physc
            * 15:53:51  0       26      18      27      29    0.35
            *           1        0       1       0      99    0.22
            *           2        0       1       0      99    0.22
            *
            * 62        0       1       0      99    0.23
            * 63        0       1       0      99    0.23
            *           -       18       6       6      70   16.04
            * 15:53:52  0       54      14      16      15    0.44
            * 1        0       1       0      99    0.19
            * 2        0       1       0      99    0.19
            * 3        0       1       0      99    0.19
            *
            * 63        0       1       0      99    0.20
            *           -       14       6       6      74   15.98
            *
            * Average   0       30      18      20      32    0.36
            * 1        0       2       0      98    0.21
            * 2        0       1       0      99    0.21
            * 3        0       1       0      99    0.21
            *           -       14       6       6      74   15.98
            */
          val titlePattern = "^\\d{1,2}:\\d{1,2}:\\d{1,2}\\s+cpu\\s+.*\\w+$".r
          val dataPattern = "^(?:\\d{1,2}:\\d{1,2}:\\d{1,2}|\\s+\\d+)\\s+\\d+\\s+.*$".r
          val splitPattern = "\\s+".r
          val titleListPattern = ListBuffer((CpuProcessorStats.cpuId._1, "^cpu$".r, CpuProcessorStats.cpuId._2),
            (CpuProcessorStats.sys._1, "^%sys$".r, CpuProcessorStats.sys._2),
            (CpuProcessorStats.ioWait._1, "^%wio$".r, CpuProcessorStats.ioWait._2),
            (CpuProcessorStats.idle._1, "^%idle$".r, CpuProcessorStats.idle._2),
            (CpuProcessorStats.user._1, "^%usr$".r, CpuProcessorStats.user._2))
          formatedData = shhShellMessgesService.formatConvertColumedMessages2Map(sshResult, titlePattern, footRegex, dataPattern, splitPattern, titleListPattern)
        }
        case "SOLARIS" => {
          /**
            * OSTYPE:SOLARIS
            * CPU usr sys wt idl
            * 0 7 1 0 92
            * 1 23 2 0 75
            *
            * 159 8 0 0 91
            * CPU usr sys wt idl
            * 0 1 1 0 98
            * 1 4 3 0 93
            * 2 21 6 0 73
            *
            */
          val titlePattern = "^CPU\\s+.*\\w+$".r
          val dataPattern = "^\\d+\\s+\\d+\\s+.*$".r
          val splitPattern = "\\s+".r
          val titleListPattern = ListBuffer((CpuProcessorStats.cpuId._1, "^CPU$".r, CpuProcessorStats.cpuId._2), (CpuProcessorStats.sys._1, "^sys$".r, CpuProcessorStats.sys._2),
            (CpuProcessorStats.ioWait._1, "^wt$".r, CpuProcessorStats.ioWait._2), (CpuProcessorStats.idle._1, "^idl$".r, CpuProcessorStats.idle._2),
            (CpuProcessorStats.user._1, "^usr$".r, CpuProcessorStats.user._2))
          formatedData = shhShellMessgesService.formatConvertColumedMessages2Map(sshResult, titlePattern, footRegex, dataPattern, splitPattern, titleListPattern)
        }
        case _ => {}
      }

      implicit def objectToInt(x: Object) = x.asInstanceOf[Int]

      implicit def objectToFloat(x: Object) = x.asInstanceOf[Float]

      var statsList: ListBuffer[(Int, Float, Float, Float, Float, Int)] = ListBuffer()
      formatedData.foreach(fd => {
        val cpuId = fd(CpuProcessorStats.cpuId._1)
        val sys = fd.getOrElse(CpuProcessorStats.sys._1, null)
        val ioWait = fd.getOrElse(CpuProcessorStats.ioWait._1, null)
        val idle = fd.getOrElse(CpuProcessorStats.idle._1, null)
        val user = fd.getOrElse(CpuProcessorStats.user._1, null)
        statsList.append((cpuId, sys, ioWait, idle, user, 1))
      })

      val tempSR = mutable.Map(statsList.groupBy(_._1).mapValues(v => {
        v.reduce((s, i) => {
          (s._1, s._2 + i._2, s._3 + i._3, s._4 + i._4, s._5 + i._5, s._6 + i._6)
        })
      }).toSeq: _*)

      statsResult.statsResult = new java.util.HashMap()
      tempSR.foreach(v => {
        statsResult.statsResult.put(v._1, (v._2._2, v._2._3, v._2._4, v._2._5, v._2._6))
      })

      logger.debug(s"cpuProcessorStatsCaptcher:${statsResult.toString()}")

      val producerRecord = new ProducerRecord(TOPIC, statsResult.targetId, statsResult)
      shhShellMessgesService.sendRecord2Kafka(producerRecord, classOf[CpuProcessorStats])
    } catch {
      case e: Exception => {
        logger.warn(s"Captcher cpusum failed", e)
      }
    }

  }
}
