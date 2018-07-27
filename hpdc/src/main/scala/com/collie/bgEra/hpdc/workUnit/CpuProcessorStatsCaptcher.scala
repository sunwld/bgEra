package com.collie.bgEra.hpdc.workUnit

import java.util

import com.collie.bgEra.cloudApp.dtsf.{ResourceManager, WorkUnitRunable}
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo
import com.collie.bgEra.cloudApp.ssh2Pool.{Ssh2Session, SshResult}
import com.collie.bgEra.commons.util.SerialNumberUtils
import com.collie.bgEra.hpdc.workUnit.bean.CpuProcessorStats
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Component

import scala.util.control.Breaks._
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

@Component("cpuProcessorStatsCaptcher")
class CpuProcessorStatsCaptcher extends WorkUnitRunable {
  private val TOPIC = "hpdc-cpu-processor"
  private val SHELL = "CPU_DETAIL.xsh"
  private val logger: Logger = LoggerFactory.getLogger("hpdc")

  @Autowired
  @Qualifier("hostShellMap")
  private val shellMap: java.util.Map[String, String] = null

  @Autowired
  private val kfkProducer: KafkaProducer[String, Object] = null

  @Autowired
  private val resManager: ResourceManager = null

  override def runWork(workUnitInfo: WorkUnitInfo): Unit = {

    val cmd = shellMap.get(SHELL)
    var session: Ssh2Session = null
    var sshResult: SshResult = null
    try {
      session = resManager.getHostSshConnPoolResource(workUnitInfo.getTargetId())
      sshResult = session.execCommand(cmd)

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

      if (sshResult.isFinishAndCmdSuccess()) {
        //OSTYPE:LINUX
        var statsList: ListBuffer[(Int, Float, Float, Float, Float, Int)] = null
        val osType = sshResult.getStrout().get(0).split(":")(1)
        osType match {
          case "LINUX" => statsList = parseLinuxResult(sshResult.getStrout())
          case "AIX" => statsList = parseAixResult(sshResult.getStrout())
          case "SOLARIS" => statsList = parseSolarisResult(sshResult.getStrout())
        }

        // L(cpuid,sysp,userp,iowaitp,idlep, 1) => M(cpuid -> sum(cpuid,sysp,userp,iowaitp,idlep,snapcount))
        statsResult.statsResult = statsList.groupBy(_._1).mapValues(v => {
          v.reduce((s, i) => {
            (i._1, s._2 + i._2, s._3 + i._3, s._4 + i._4, s._5 + i._5, s._6 + i._6)
          })
        })

        val record: ProducerRecord[String, Object] = new ProducerRecord(TOPIC, workUnitInfo.targetId, statsResult)
        kfkProducer.send(record)
      }

    } catch {
      case e: Exception => {
        logger.warn(s"Captcher cpusum failed, shell result[${sshResult}]", e)
      }
    } finally {
      if (session != null) {
        session.close()
      }
    }

  }

  private def parseLinuxResult(resLines: util.List[String]): ListBuffer[(Int, Float, Float, Float, Float, Int)] = {
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
    val linuxOsInfoParten = "^Linux".r
    val linuxCpuDataParten = "^\\d{1,2}:\\d{1,2}:\\d{1,2}".r
    val numumberParten = "^\\d".r
    val dataLineSplitStr = "\\s{1,}|\\t{1,}".r

    val statsList: ListBuffer[(Int, Float, Float, Float, Float, Int)] = ListBuffer()

    resLines.foreach(line => {
      if (!linuxCpuDataParten.findFirstIn(line).isEmpty) {
        /**
          * 05:34:50 PM     CPU     %user     %nice   %system   %iowait    %steal     %idle
          * 05:34:52 PM     all      0.13      0.00      0.13      0.00      0.00     99.75
          * 05:34:52 PM       0      0.00      0.00      0.00      0.00      0.00    100.00
          */
        val lineItems = dataLineSplitStr.split(line)
        if (!numumberParten.findFirstIn(lineItems.last).isEmpty) {
          /**
            * 05:34:52 PM     all      0.13      0.00      0.13      0.00      0.00     99.75
            * 05:34:52 PM       0      0.00      0.00      0.00      0.00      0.00    100.00
            */
          if (!numumberParten.findFirstIn(lineItems(cpuPcStatsColMap.get("cpuidp"))).isEmpty) {
            // 05:34:52 PM       0      0.00      0.00      0.00      0.00      0.00    100.00
            val oneStats: (Int, Float, Float, Float, Float, Int) = (lineItems(cpuPcStatsColMap.get("cpuidp")).toInt,
              lineItems(cpuPcStatsColMap.get("sysp")).toFloat,
              lineItems(cpuPcStatsColMap.get("userp")).toFloat,
              lineItems(cpuPcStatsColMap.get("iowaitp")).toFloat,
              lineItems(cpuPcStatsColMap.get("idlep")).toFloat, 1)
            statsList.append(oneStats)
          } else {
            // 05:34:52 PM     all      0.13      0.00      0.13      0.00      0.00     99.75
          }
        } else {
          // 05:34:50 PM     CPU     %user     %nice   %system   %iowait    %steal     %idle
          mapCpuPcStatsCols(lineItems)
        }

      } else {
        /**
          * Linux 3.10.0-693.el7.x86_64 (devops1)   07/24/2018      _x86_64_        (16 CPU)
          * Average:         13      0.20      0.00      0.20      0.00      0.00     99.60
          * Average:         14      0.20      0.00      0.00      0.00      0.00     99.80
          * Average:         15      0.20      0.00      0.00      0.00      0.00     99.80
          */
      }
    })
    statsList
  }

  private def parseAixResult(resLines: util.List[String]): ListBuffer[(Int, Float, Float, Float, Float, Int)] = {
    /**
      * AIX bildb1 1 6 00F69A714C00    07/24/18
      *
      * System configuration: lcpu=64  mode=Capped
      *
      * 15:53:50 cpu    %usr    %sys    %wio   %idle   physc
      * 15:53:51  0       26      18      27      29    0.35
      * 1        0       1       0      99    0.22
      * 2        0       1       0      99    0.22
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
    val aixOsInfoParten = "(^AIX)|(^System)".r
    val aixCpuDataParten = "(^\\d{1,2}:\\d{1,2}:\\d{1,2})|(^\\s{1,}|\\t{1,})".r
    val aixTimeColPartem = "^\\d{1,2}:\\d{1,2}:\\d{1,2}".r
    val numumberParten = "^\\d".r
    val dataLineSplitStr = "\\s{1,}|\\t{1,}".r

    val statsList: ListBuffer[(Int, Float, Float, Float, Float, Int)] = ListBuffer()

    breakable {
      resLines.foreach(line => {
        if (!aixCpuDataParten.findFirstIn(line).isEmpty) {
          /**
            * 15:53:50 cpu    %usr    %sys    %wio   %idle   physc
            * 15:53:51  0       26      18      27      29    0.35
            * 1        0       1       0      99    0.22
            *           -       18       6       6      70   16.04
            */
          val lineItems = dataLineSplitStr.split(line)
          if (!numumberParten.findFirstIn(lineItems.last).isEmpty) {
            /**
              * 15:53:51  0       26      18      27      29    0.35
              * 1        0       1       0      99    0.22
              *           -       18       6       6      70   16.04
              */
            if (!numumberParten.findFirstIn(lineItems(cpuPcStatsColMap.get("cpuidp"))).isEmpty) {
              /**
                * * 15:53:51  0       26      18      27      29    0.35
                * *           1        0       1       0      99    0.22
                */
              val oneStats: (Int, Float, Float, Float, Float, Int) = (lineItems(cpuPcStatsColMap.get("cpuidp")).toInt,
                lineItems(cpuPcStatsColMap.get("sysp")).toFloat,
                lineItems(cpuPcStatsColMap.get("userp")).toFloat,
                lineItems(cpuPcStatsColMap.get("iowaitp")).toFloat,
                lineItems(cpuPcStatsColMap.get("idlep")).toFloat, 1)
              statsList.append(oneStats)
            } else {
              //           -       18       6       6      70   16.04
            }

          } else {
            //  15:53:50 cpu    %usr    %sys    %wio   %idle   physc
            mapCpuPcStatsCols(lineItems)
          }

        } else {
          /**
            * Linux 3.10.0-693.el7.x86_64 (devops1)   07/24/2018      _x86_64_        (16 CPU)
            * Average:         13      0.20      0.00      0.20      0.00      0.00     99.60
            * Average:         14      0.20      0.00      0.00      0.00      0.00     99.80
            * Average:         15      0.20      0.00      0.00      0.00      0.00     99.80
            */
          if (line.startsWith("Ave") || line.startsWith("ave")) {
            break()
          }
        }
      })
    }
    statsList
  }

  private def parseSolarisResult(resLines: util.List[String]): ListBuffer[(Int, Float, Float, Float, Float, Int)] = {
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
    val numumberParten = "^\\d".r
    val dataLineSplitStr = "\\s{1,}|\\t{1,}".r

    val statsList: ListBuffer[(Int, Float, Float, Float, Float, Int)] = ListBuffer()

    resLines.foreach(line => {
      if (line != null && !"".equals(line)) {
        val lineItems = dataLineSplitStr.split(line)
        if (!numumberParten.findFirstIn(lineItems.last).isEmpty) {
          /**
            * 0 7 1 0 92
            * 1 23 2 0 75
            */
          val oneStats: (Int, Float, Float, Float, Float, Int) = (lineItems(cpuPcStatsColMap.get("cpuidp")).toInt,
            lineItems(cpuPcStatsColMap.get("sysp")).toFloat,
            lineItems(cpuPcStatsColMap.get("userp")).toFloat,
            lineItems(cpuPcStatsColMap.get("iowaitp")).toFloat,
            lineItems(cpuPcStatsColMap.get("idlep")).toFloat, 1)
          statsList.append(oneStats)
        } else {
          //  CPU usr sys wt idl
          mapCpuPcStatsCols(lineItems)
        }
      }
    })
    statsList
  }

  private val cpuPcStatsColMap: java.util.Map[String, Int] = new util.HashMap()

  private def mapCpuPcStatsCols(lineItems: Array[String]): Unit = {
    for (i <- 0 until lineItems.length) {
      val s = lineItems(i)
      if (s.contains("user") || s.contains("usr") || s.contains("%u")) {
        cpuPcStatsColMap.put("userp", i)
      } else if (s.contains("sys") || s.contains("sy")) {
        cpuPcStatsColMap.put("sysp", i)
      } else if (s.contains("wt") || s.contains("iowait") || s.contains("wait") || s.contains("wa") || s.contains("iow")) {
        cpuPcStatsColMap.put("iowaitp", i)
      } else if (s.contains("idle") || s.contains("idl") || s.contains("id")) {
        cpuPcStatsColMap.put("idlep", i)
      } else if (s.contains("CPU") || s.contains("cpu") || s.contains("Cpu")) {
        cpuPcStatsColMap.put("cpuidp", i)
      }
    }
  }
}
