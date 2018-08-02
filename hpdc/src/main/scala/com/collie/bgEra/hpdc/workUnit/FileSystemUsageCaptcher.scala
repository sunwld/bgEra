package com.collie.bgEra.hpdc.workUnit

import java.util

import com.collie.bgEra.cloudApp.dtsf.{ResourceManager, WorkUnitRunable}
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo
import com.collie.bgEra.cloudApp.ssh2Pool.{Ssh2Session, SshResult}
import com.collie.bgEra.commons.util.{SerialNumberUtils, StringUtils}
import com.collie.bgEra.hpdc.context.KafkaProducerSource
import com.collie.bgEra.hpdc.service.ShhShellMessgesService
import com.collie.bgEra.hpdc.service.bean.ShellInfo
import com.collie.bgEra.hpdc.workUnit.bean.{CpuProcessorStats, FilesystemUsageStats}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex

@Component("fileSystemUsageCaptcher")
class FileSystemUsageCaptcher extends WorkUnitRunable {
  private val TOPIC = "hpdc-fsusage"
  private val SHELL = "FSSTAT.xsh"

  private val logger: Logger = LoggerFactory.getLogger("hpdc")

  @Autowired
  private val shhShellMessgesService: ShhShellMessgesService = null

  override def runWork(workUnitInfo: WorkUnitInfo): Unit = {


    /**
      * OSTYPE:AIX
      * Filesystem    1024-blocks      Used Available Capacity Mounted on
      * /dev/hd4          6291456   2792528   3498928      45% /
      * /dev/hd2          8388608   5703796   2684812      68% /usr
      * /dev/hd9var       6291456   2241348   4050108      36% /var
      * /dev/hd3         10485760   5137888   5347872      49% /tmp
      * /dev/hd1          6291456    928996   5362460      15% /home
      * /dev/hd11admin      524288       424    523864       1% /admin
      * /proc                   -         -         -       -  /proc
      * /dev/hd10opt      4194304   1908104   2286200      46% /opt
      * /dev/livedump      524288       408    523880       1% /var/adm/ras/livedump
      * /dev/oraclelv    41943040  26489588  15453452      64% /oracle
      * /dev/patrollv     2097152     34728   2062424       2% /patrol
      * /dev/combaklv     5242880   2670320   2572560      51% /combak
      * /dev/odm                0         0         0      -1% /dev/odm
      * /dev/vx/dsk/dg_bildb1/volvoting      512000     37120    474880       8% /bilvoting
      * /dev/vx/dsk/dg_bildb1/volocr      512000     18240    493760       4% /bilocr
      * /dev/vx/dsk/dg_bildb1/volbilindx2  2359296000 2217340096 141955904      94% /bilindx2
      * /dev/vx/dsk/dg_bildb1/volbildata5  3670016000 3419718760 250297240      94% /bildata5
      * /dev/vx/dsk/dg_bildb1/volbilindx1  2097152000 1941345864 155806136      93% /bilindx1
      * /dev/vx/dsk/dg_bildb1/volbilindx3  2202009600 2067423344 134586256      94% /bilindx3
      * /dev/vx/dsk/dg_bildb1/volbildata4  3670016000 3411700480 258315520      93% /bildata4
      * /dev/vx/dsk/dg_bildb1/volbildata6  3670016000 3415492160 254523840      94% /bildata6
      * /dev/vx/dsk/dg_bildb1/volbilsysdata   943718400 869726559  73991841      93% /bilsysdata
      * /dev/vx/dsk/dg_bildb1/volbildata3  4194304000 3913240640 281063360      94% /bildata3
      * /dev/vx/dsk/dg_bildb1/volbildata2  3670016000 3452104952 217911048      95% /bildata2
      * /dev/vx/dsk/dg_bildb1/volbildata1  4194304000 4014829960 179474040      96% /bildata1
      * /dev/vx/dsk/dg_bildb1/volbilarch1   524288000  58542416 465745584      12% /bilarch11
      */

    /**
      * OSTYPE:SOLARIS
      * Filesystem           1024-blocks        Used   Available Capacity  Mounted on
      * rpool/ROOT/sllsru205   511967232    19902056   296156080     7%    /
      * /devices                       0           0           0     0%    /devices
      * /dev                           0           0           0     0%    /dev
      * ctfs                           0           0           0     0%    /system/contract
      * proc                           0           0           0     0%    /proc
      * mnttab                         0           0           0     0%    /etc/mnttab
      * swap                    29667248        2648    29664600     1%    /system/volatile
      * objfs                          0           0           0     0%    /system/object
      * sharefs                        0           0           0     0%    /etc/dfs/sharetab
      * fd                             0           0           0     0%    /dev/fd
      * rpool/ROOT/sllsru205/var
      * 511967232      668048   296156080     1%    /var
      * swap                    29667328        2728    29664600     1%    /tmp
      * rpool/VARSHARE         511967232     9695888   296156080     4%    /var/share
      * /dev/dsk/c4t604F9381009445741D753BE10000000Bd0s6
      * 242926889     5777752   234719869     3%    /arch
      * rpool/export           511967232         448   296156080     1%    /export
      * rpool/export/home      511967232         320   296156080     1%    /export/home
      * rpool/export/home/grid
      * 511967232    20884160   296156080     7%    /export/home/grid
      * rpool/export/home/oracle
      * 511967232    13814040   296156080     5%    /export/home/oracle
      * rpool                  511967232         384   296156080     1%    /rpool
      */

    try {
      val sshResult = shhShellMessgesService.loadShellResults(new ShellInfo(SHELL, workUnitInfo.targetId, mutable.Map()))
      if (sshResult == null || sshResult.isEmpty) {
        return
      }

      var statsResult: FilesystemUsageStats = new FilesystemUsageStats()
      statsResult.targetId = workUnitInfo.targetId
      statsResult.snapId = SerialNumberUtils.getSerialByTrunc1min(workUnitInfo.thisTime, true)
      statsResult.statsResult = new java.util.HashMap()

      val osType = sshResult.get(0).split(":")(1)

      val dataLineSplitStr = "\\s+".r
      val dataLineSplitStrHead = "^\\s+".r
      val numumberParten = "\\s+\\d+\\.?\\d*\\s+".r
      val footRegex = "^[Aa]verage:?\\s+.*$".r

      var tidyShellResultLines: ListBuffer[String] = ListBuffer()
      sshResult.foreach(line => {
        /**
          * rpool/VARSHARE         511967232     9695888   296156080     4%    /var/share
          * rpool/export/home/grid
          * 511967232    20884160   296156080     7%    /export/home/grid
          */
        if (!dataLineSplitStrHead.findFirstIn(line).isEmpty) {
          //     511967232    20884160   296156080     7%    /export/home/grid
          val integLine = tidyShellResultLines.trimEnd(1) + line
          tidyShellResultLines += integLine
        } else {
          //rpool/VARSHARE         511967232     9695888   296156080     4%    /var/share
          tidyShellResultLines += line
        }
      })

      val titleLineRegex = "^Filesystem\\s+.*$".r
      val dataLineRegex = "^.*\\s+\\d+\\.?\\d*\\s+.*$".r
      val titleListRegex = ListBuffer[(String, Regex)](("fsName", "^Filesystem$".r), ("mountPoint", "^Mounted".r), ("useKb", "^Used$".r), ("freeKb", "^Available$".r),
        ("usedPerc", "^Capacity$".r))
      val formatedData = shhShellMessgesService.formatColumedMessages2Map(sshResult, titleLineRegex, footRegex, dataLineRegex, dataLineSplitStr, titleListRegex)

      var fsName, mountPoint: String = ""
      var freeKb, usedKb: Double = 0D
      var usePerc: Float = 0F
      formatedData.foreach(fd => {
        fsName = fd("fsName")
        mountPoint = fd.getOrElse("mountPoint", null)
        freeKb = StringUtils.toDouble(fd.getOrElse("freeKb", null))
        usedKb = StringUtils.toDouble(fd.getOrElse("useKb", null))
        usePerc = StringUtils.toFloat(fd.getOrElse("usedPerc", "").replace("%", ""))
        statsResult.statsResult.put(fsName, (mountPoint, freeKb, usedKb, usePerc))
      })

      logger.debug(s"fileSystemUsageCaptcher:${statsResult.toString()}")

      val producerRecord = new ProducerRecord(TOPIC, statsResult.targetId, statsResult)
      shhShellMessgesService.sendRecord2Kafka(producerRecord, classOf[FilesystemUsageStats])
    } catch {
      case e: Exception => {
        logger.warn(s"Captcher fsUsage failed.", e)
      }
    }

  }
}
