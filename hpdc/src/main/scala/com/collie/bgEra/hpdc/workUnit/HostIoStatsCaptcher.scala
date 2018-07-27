package com.collie.bgEra.hpdc.workUnit

import java.util

import com.collie.bgEra.cloudApp.dtsf.{ResourceManager, WorkUnitRunable}
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo
import com.collie.bgEra.cloudApp.ssh2Pool.{Ssh2Session, SshResult}
import com.collie.bgEra.commons.util.SerialNumberUtils
import com.collie.bgEra.hpdc.workUnit.bean.HostIOStats
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._
import scala.util.control.Breaks._
import scala.collection.mutable.{ListBuffer}

@Component("hostIoStatsCaptcher")
class HostIoStatsCaptcher extends WorkUnitRunable {
  private val TOPIC = "hpdc-iostat"
  private val SHELL = "IOSTAT"

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
        * K: Device name
        * V:(DeviceName,IOKB,avgWait,busy%,snapCount)
        *
        */
      val hostIoStats: HostIOStats = new HostIOStats()
      hostIoStats.snapId = SerialNumberUtils.getSerialByTrunc10s(workUnitInfo.thisTime, true)
      hostIoStats.targetId = workUnitInfo.targetId

      if (sshResult.isFinishAndCmdSuccess()) {
        var statsList: ListBuffer[(String, Double, Double, Float, Int)] = null
        val osType = sshResult.getStrout().get(0).split(":")(1)
        osType match {
          case "LINUX" => statsList = parseLinuxResult(sshResult.getStrout())
          case "AIX" => statsList = parseAixResult(sshResult.getStrout())
          case "SOLARIS" => statsList = parseSolarisResult(sshResult.getStrout())
        }

        hostIoStats.statsResult = statsList.groupBy(_._1).mapValues(v => {
          v.reduce((s, i) => {
            (i._1, s._2 + i._2, s._3 + i._3, s._4 + i._4, s._5 + i._5)
          })
        })

        val record: ProducerRecord[String, Object] = new ProducerRecord(TOPIC, workUnitInfo.targetId, hostIoStats)
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

  private def parseLinuxResult(resLines: util.List[String]): ListBuffer[(String, Double, Double, Float, Int)] = {
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

    val linuxDataParten = "(^Device:)|(\\d$)".r
    val numumberParten = "^\\d".r
    val dataLineSplitStr = "(\\s{1,})|(\\t{1,})".r

    //(DeviceName,IOKB,avgWait,busy%,snapCount)
    val statRes: ListBuffer[(String, Double, Double, Float, Int)] = ListBuffer()

    resLines.foreach(line => {
      if (!linuxDataParten.findFirstIn(line).isEmpty) {
        /**
          * Device:         rrqm/s   wrqm/s     r/s     w/s    rkB/s    wkB/s avgrq-sz avgqu-sz   await r_await w_await  svctm  %util
          * dm-0              0.00     0.00    0.01    0.73     1.80     5.39    19.39     0.00    2.77    4.71    2.75   1.29   0.10
          * dm-3              0.00     0.00    0.00   12.00     0.00    48.00     8.00     0.01    0.92    0.00    0.92   0.92   1.10
          */
        var rkbp: Int = -1
        var wkbp: Int = -1
        var devp: Int = -1
        var avwaitp: Int = -1
        var busyp: Int = -1

        val datItems = dataLineSplitStr.split(line)
        if (datItems.length > 5 && !datItems(0).equals("Device:")) {
          //dm-0              0.00     0.00    0.01    0.73     1.80     5.39    19.39     0.00    2.77    4.71    2.75   1.29   0.10
          statRes.append((datItems(devp),
            datItems(rkbp).toDouble + datItems(wkbp).toDouble,
            datItems(avwaitp).toDouble,
            datItems(busyp).toFloat,
            1))
        } else if (datItems.length > 5 && datItems(0).equals("Device:")) {
          //Device:         rrqm/s   wrqm/s     r/s     w/s    rkB/s    wkB/s avgrq-sz avgqu-sz   await r_await w_await  svctm  %util
          for (i <- 0 until datItems.length) {
            if (datItems(i).startsWith("rkB") || datItems(i).startsWith("rkb")) {
              rkbp = i
            } else if (datItems(i).startsWith("wkB") || datItems(i).startsWith("wkb")) {
              wkbp = i
            } else if (datItems(i).startsWith("Dev") || datItems(i).startsWith("dev")) {
              devp = i
            } else if (datItems(i).startsWith("await") || datItems(i).startsWith("Awai")) {
              avwaitp = i
            } else if (datItems(i).contains("util") || datItems(i).startsWith("busy")) {
              busyp = i
            }
          }
        }
      } else {
        //Linux 3.10.0-693.el7.x86_64 (devops1)   07/24/2018      _x86_64_        (16 CPU)
      }
    })
    statRes
  }

  private def parseAixResult(resLines: util.List[String]): ListBuffer[(String, Double, Double, Float, Int)] = {
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

    val aixOsInfoParten = "(^AIX)|(^System)".r
    val aixDataParten = "(^\\d{1,2}:\\d{1,2}:\\d{1,2})|(^\\s+)".r
    val aixTimeColPartem = "^\\d{1,2}:\\d{1,2}:\\d{1,2}".r
    val numumberParten = "^\\d".r
    val dataLineSplitStr = "\\s{1,}|\\t{1,}".r


    //(DeviceName,IOKB,avgWait,busy%,snapCount)
    val statRes: ListBuffer[(String, Double, Double, Float, Int)] = ListBuffer()

    breakable {
      resLines.foreach(line => {
        if (!aixDataParten.findFirstIn(line).isEmpty) {
          /*
            * 11:35:09     device    %busy    avque    r+w/s    Kbs/s   avwait   avserv
            *
            * 11:35:10     hdisk3      0      0.0        0        0      0.0      0.0
            *              hdisk1     72      0.0      173      692      0.0      4.6
            */
          var kbp: Int = -1
          var devp: Int = -1
          var avwaitp: Int = -1
          var busyp: Int = -1

          val datItems = dataLineSplitStr.split(line)

          if (!numumberParten.findFirstIn(datItems.last).isEmpty) {
            //11:35:10     hdisk3      0      0.0        0        0      0.0      0.0
            //             hdisk1     72      0.0      173      692      0.0      4.6
            statRes.append((datItems(devp),
              datItems(kbp).toDouble,
              datItems(avwaitp).toDouble,
              datItems(busyp).toFloat,
              1))

          } else {
            //11:35:09     device    %busy    avque    r+w/s    Kbs/s   avwait   avserv
            for (i <- 0 until datItems.length) {
              if (datItems(i).startsWith("Kbs") || datItems(i).startsWith("kbs")) {
                kbp = i
              } else if (datItems(i).startsWith("dev") || datItems(i).startsWith("Dev")) {
                devp = i
              } else if (datItems(i).startsWith("avs") || datItems(i).startsWith("Avs")) {
                avwaitp = i
              } else if (datItems(i).contains("busy") || datItems(i).startsWith("util")) {
                busyp = i
              }
            }
          }
        } else {
          /**
            * Average      hdisk3      0      0.0        0        0      0.0      0.0
            * * hdisk1      1      0.0        2        8      0.0      3.8
            * * hdisk0      2      0.0        2        8      0.0      3.9
            */
          if (line.startsWith("Ave") || line.startsWith("ave")) {
            break()
          }
        }
      })
    }
    statRes
  }

  private def parseSolarisResult(resLines: util.List[String]): ListBuffer[(String, Double, Double, Float, Int)] = {
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

    val solarisDataParten = "(^\\d{1,2}:\\d{1,2}:\\d{1,2})|(^\\s{1,}|\\t{1,})".r
    val solarisTimeColPartem = "^\\d{1,2}:\\d{1,2}:\\d{1,2}".r
    val numumberParten = "^\\d".r
    val dataLineSplitStr = "\\s{1,}|\\t{1,}".r


    //(DeviceName,IOKB,avgWait,busy%,snapCount)
    val statRes: ListBuffer[(String, Double, Double, Float, Int)] = ListBuffer()

    breakable {
      resLines.foreach(line => {
        if (!solarisDataParten.findFirstIn(line).isEmpty) {
          // 11:42:23   device        %busy   avque   r+w/s  blks/s  avwait  avserv
          //
          // 11:42:24   fp0               0     0.0       0       0     0.0     0.0
          //            fp2              99     2.8    1050  117269     0.0     2.6
          //            sd5              13     0.1     103    7737     0.0     1.4
          //            sd5,a             0     0.0       0       0     0.0     0.0
          //            sd5,h             0     0.0       1      16     0.0     0.4
          //            sd7              33     0.5     169   15364     0.0     2.8
          var kbp: Int = -1
          var devp: Int = -1
          var avwaitp: Int = -1
          var busyp: Int = -1

          val datItems = dataLineSplitStr.split(line)

          if (!numumberParten.findFirstIn(datItems.last).isEmpty) {
            // 11:42:24   fp0               0     0.0       0       0     0.0     0.0
            //            fp2              99     2.8    1050  117269     0.0     2.6
            //            sd5,a             0     0.0       0       0     0.0     0.0
            //            sd5,h             0     0.0       1      16     0.0     0.4
            if (!datItems(devp).contains(",")) {
              statRes.append((datItems(devp),
                datItems(kbp).toDouble / 2,
                datItems(avwaitp).toDouble,
                datItems(busyp).toFloat,
                1))
            }

          } else {
            // 11:42:23   device        %busy   avque   r+w/s  blks/s  avwait  avserv
            for (i <- 0 until datItems.length) {
              if (datItems(i).startsWith("blks") || datItems(i).startsWith("Blks")) {
                kbp = i
              } else if (datItems(i).startsWith("dev") || datItems(i).startsWith("Dev")) {
                devp = i
              } else if (datItems(i).startsWith("avs") || datItems(i).startsWith("Avs")) {
                avwaitp = i
              } else if (datItems(i).contains("busy") || datItems(i).startsWith("util")) {
                busyp = i
              }
            }
          }
        } else {
          // Average    fp0               0     0.0       0       0     0.0     0.00
          //            fp2              99     2.5    1091   74918     0.0     2.3
          //            vdc1              0     0.0       0       0     0.0     0.0
          if (line.startsWith("Ave") || line.startsWith("ave")) {
            break()
          }
        }
      })
    }
    statRes
  }

}
