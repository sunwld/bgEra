package com.collie.bgEra.hpdc.workUnit

import java.util

import com.collie.bgEra.cloudApp.dtsf.{ResourceManager, WorkUnitRunable}
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo
import com.collie.bgEra.cloudApp.ssh2Pool.{Ssh2Session, SshResult}
import com.collie.bgEra.commons.util.SerialNumberUtils
import com.collie.bgEra.hpdc.workUnit.bean.{CpuProcessorStats, CpuStats}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import scala.collection.JavaConversions._

import scala.util.control.Breaks.{break, breakable}

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

    val cmd = shellMap.get("CPU.xsh")
    var session: Ssh2Session = null
    var sshResult: SshResult = null
    try {
      session = resManager.getHostSshConnPoolResource(workUnitInfo.getTargetId())
      sshResult = session.execCommand(cmd)

      if (sshResult.isFinishAndCmdSuccess()) {

        /**
          * OSTYPE:LINUX
          * Linux 3.10.0-693.el7.x86_64 (devops1)   07/24/2018      _x86_64_        (16 CPU)
          *
          * 03:51:59 PM     CPU     %user     %nice   %system   %iowait    %steal     %idle
          * 03:52:00 PM     all      0.19      0.00      0.06      0.06      0.00     99.69
          * 03:52:00 PM       0      0.00      0.00      0.00      0.00      0.00    100.00
          * 03:52:00 PM       1      0.99      0.00      0.00      0.99      0.00     98.02
          * 03:52:00 PM       2      0.00      0.00      0.00      0.00      0.00    100.00
          * 03:52:00 PM       3      1.00      0.00      0.00      0.00      0.00     99.00
          * 03:52:00 PM       4      0.00      0.00      0.00      0.00      0.00    100.00
          * 03:52:00 PM       5      0.00      0.00      0.00      0.00      0.00    100.00
          * 03:52:00 PM       6      0.00      0.00      0.00      0.00      0.00    100.00
          * 03:52:00 PM       7      0.00      0.00      0.00      0.00      0.00    100.00
          * 03:52:00 PM       8      0.00      0.00      0.00      0.00      0.00    100.00
          * 03:52:00 PM       9      0.00      0.00      1.00      0.00      0.00     99.00
          * 03:52:00 PM      10      0.00      0.00      0.00      0.00      0.00    100.00
          * 03:52:00 PM      11      0.00      0.00      0.00      0.00      0.00    100.00
          * 03:52:00 PM      12      0.00      0.00      0.00      0.00      0.00    100.00
          * 03:52:00 PM      13      0.00      0.00      0.00      0.00      0.00    100.00
          * 03:52:00 PM      14      0.00      0.00      0.00      0.00      0.00    100.00
          * 03:52:00 PM      15      0.00      0.00      0.00      0.00      0.00    100.00
          *
          * 03:52:00 PM     CPU     %user     %nice   %system   %iowait    %steal     %idle
          * 03:52:01 PM     all      0.06      0.00      0.06      0.00      0.00     99.87
          * 03:52:01 PM       0      0.00      0.00      0.00      0.00      0.00    100.00
          * 03:52:01 PM       1      0.00      0.00      0.00      0.00      0.00    100.00
          * 03:52:01 PM       2      0.00      0.00      0.00      0.00      0.00    100.00
          * *** ***
          * Average:        CPU     %user     %nice   %system   %iowait    %steal     %idle
          * Average:        all      0.19      0.00      0.15      0.03      0.00     99.64
          * Average:          0      0.00      0.00      0.20      0.00      0.00     99.80
          * Average:          1      0.20      0.00      0.00      0.20      0.00     99.60
          * Average:          2      0.20      0.00      0.00      0.00      0.00     99.80
          * Average:          3      0.60      0.00      0.20      0.00      0.00     99.20
          * Average:          4      0.20      0.00      0.20      0.00      0.00     99.60
          * Average:          5      0.00      0.00      0.20      0.20      0.00     99.60
          * Average:          6      0.00      0.00      0.20      0.00      0.00     99.80
          * Average:          7      0.20      0.00      0.00      0.00      0.00     99.80
          * Average:          8      0.00      0.00      0.00      0.00      0.00    100.00
          * Average:          9      0.20      0.00      0.20      0.00      0.00     99.60
          * Average:         10      0.20      0.00      0.00      0.00      0.00     99.80
          * Average:         11      0.00      0.00      0.20      0.00      0.00     99.80
          * Average:         12      0.40      0.00      0.20      0.00      0.00     99.40
          * Average:         13      0.60      0.00      0.00      0.00      0.00     99.40
          * Average:         14      0.00      0.00      0.20      0.00      0.00     99.80
          * Average:         15      0.00      0.00      0.20      0.00      0.00     99.80
          */
        //val record: ProducerRecord[String, Object] = new ProducerRecord(TOPIC, workUnitInfo.targetId, cpu)
        val cpu = new CpuProcessorStats()
        val resLine: util.List[String] = sshResult.getStrout()

        var resPart :String = ""
        var osType :String = null

        resLine.foreach(line => {

        })

        //kfkProducer.send(record)
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


    /**
      * OSTYPE:AIX
      *
      * AIX bildb1 1 6 00F69A714C00    07/24/18
      *
      * System configuration: lcpu=64  mode=Capped
      *
      * 15:53:50 cpu    %usr    %sys    %wio   %idle   physc
      * 15:53:51  0       26      18      27      29    0.35
      * 1        0       1       0      99    0.22
      * 2        0       1       0      99    0.22
      * 3        0       1       0      99    0.22
      * 4       52       5      16      27    0.39
      * *** ***
      * 60       26       6      26      42    0.31
      * 61        0       1       0      99    0.23
      * 62        0       1       0      99    0.23
      * 63        0       1       0      99    0.23
      *           -       18       6       6      70   16.04
      * 15:53:52  0       54      14      16      15    0.44
      * 1        0       1       0      99    0.19
      * 2        0       1       0      99    0.19
      * 3        0       1       0      99    0.19
      * 4       24       9      35      32    0.32
      * 5        0       1       0      99    0.23
      * *** ***
      * 61        0       1       0      99    0.20
      * 62        0       1       0      99    0.20
      * 63        0       1       0      99    0.20
      *           -       14       6       6      74   15.98
      *
      * Average   0       30      18      20      32    0.36
      * 1        0       2       0      98    0.21
      * 2        0       1       0      99    0.21
      * 3        0       1       0      99    0.21
      * 4       29       8      24      39    0.33
      * 5        0       1       0      99    0.22
      * 6        0       1       0      99    0.22
      * 7        0       1       0      99    0.22
      * *** ***
      * 58        0       1       0      99    0.23
      * 59        0       1       0      99    0.23
      * 60       40       5      24      32    0.35
      * 61        0       1       0      99    0.22
      * 62        0       1       0      99    0.22
      * 63        0       1       0      99    0.22
      *           -       15       6       7      72   16.00
      */


    /**
      * OSTYPE:SOLARIS
      * CPU usr sys wt idl
      * 0 7 1 0 92
      * 1 23 2 0 75
      * 2 6 2 0 92
      * 3 3 1 0 97
      * 4 3 0 0 97
      * 5 3 0 0 97
      * *** ***
      * 157 8 1 0 92
      * 158 8 0 0 92
      * 159 8 0 0 91
      * CPU usr sys wt idl
      * 0 1 1 0 98
      * 1 6 3 0 91
      * 2 1 6 0 93
      * 3 0 1 0 99
      * 4 1 2 0 97
      * *** ***
      * 150 2 1 0 97
      * 151 2 1 0 97
      * 152 2 1 0 97
      * 153 1 2 0 97
      * 154 5 1 0 94
      * 155 1 1 0 98
      * 156 2 1 0 97
      * 157 1 2 0 97
      * 158 3 1 0 96
      * 159 4 1 0 95
      */


  }
}
