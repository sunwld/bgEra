package com.collie.bgEra.hpdc.workUnit

import java.util

import com.collie.bgEra.cloudApp.dtsf.{ResourceManager, WorkUnitRunable}
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo
import com.collie.bgEra.cloudApp.ssh2Pool.{Ssh2Session, SshResult}
import com.collie.bgEra.commons.util.SerialNumberUtils
import com.collie.bgEra.hpdc.workUnit.bean.FilesystemUsageStats
import org.apache.kafka.clients.producer.KafkaProducer
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}

import scala.collection.JavaConversions._
import scala.collection.mutable.{ListBuffer}

class FileSystemUsageCaptcher extends WorkUnitRunable {
  private val TOPIC = "hpdc-fsusage"
  private val SHELL = "FSSTAT"

  private val logger: Logger = LoggerFactory.getLogger("hpdc")

  @Autowired
  @Qualifier("hostShellMap")
  private val shellMap: java.util.Map[String, String] = null

  @Autowired
  private val kfkProducer: KafkaProducer[String, Object] = null

  @Autowired
  private val resManager: ResourceManager = null

  override def runWork(workUnitInfo: WorkUnitInfo): Unit = {

    /**
      * OSTYPE:LINUX
      * Filesystem                 1024-blocks    Used  Available Capacity Mounted on
      * /dev/mapper/rhel-root         44014596 8947372   35067224      21% /
      * devtmpfs                      16454488       0   16454488       0% /dev
      * tmpfs                         16470060       0   16470060       0% /dev/shm
      * tmpfs                         16470060  238588   16231472       2% /run
      * tmpfs                         16470060       0   16470060       0% /sys/fs/cgroup
      * /dev/sda1                      2086912  192728    1894184      10% /boot
      * /dev/mapper/datavg-mysqllv  1048064000 7947616 1040116384       1% /mysqldata
      * /dev/mapper/datavg-datalv    209510452 2117180  207393272       2% /bigdata
      * tmpfs                          3294012       0    3294012       0% /run/user/0
      * tmpfs                            61440       0      61440       0% /var/log/rtlog
      * /dev/loop0                     3963760 3963760          0     100% /var/www/html/redhat74
      * tmpfs                          3294012       0    3294012       0% /run/user/1002
      * tmpfs                          3294012       0    3294012       0% /run/user/1003
      */


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

    val cmd = shellMap.get(SHELL)
    var session: Ssh2Session = null
    var sshResult: SshResult = null

    try {
      session = resManager.getHostSshConnPoolResource(workUnitInfo.getTargetId())
      sshResult = session.execCommand(cmd)

      var statsResult: FilesystemUsageStats = new FilesystemUsageStats()
      statsResult.targetId = workUnitInfo.targetId
      statsResult.snapId = SerialNumberUtils.getSerialByTrunc1min(workUnitInfo.thisTime, true)

      if (sshResult.isFinishAndCmdSuccess()) {
        val numumberParten = "^\\d".r
        val dataLineSplitStr = "\\s{1,}|\\t{1,}".r
        val dataLineSplitStrHead = "(^\\s{1,})|(^\\t{1,})".r

        var tidyShellResultLines: ListBuffer[String] = ListBuffer()
        sshResult.getStrout.foreach(line => {
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

        tidyShellResultLines.foreach(line => {
          val lis = dataLineSplitStr.split(line)

          if (!numumberParten.findFirstIn(lis.takeRight(4)(0)).isEmpty) {
            //  /dev/vx/dsk/dg_bildb1/volbildata5  3670016000 3419718760 250297240      94% /bildata5
            statsResult.statsResult.put(lis(fsUsageStatsColMap.get("fsName")),
              (
                lis(fsUsageStatsColMap.get("fsName")),
                lis(fsUsageStatsColMap.get("moutDir")),
                lis(fsUsageStatsColMap.get("usedKb")).toDouble,
                lis(fsUsageStatsColMap.get("freeKb")).toDouble,
                lis(fsUsageStatsColMap.get("freePerc")).toFloat
              ))
          } else {
            //  Filesystem           1024-blocks        Used   Available Capacity  Mounted on
            mapCpuPcStatsCols(lis)
          }

        })
      }

    } catch {
      case e: Exception => {
        logger.warn(s"Captcher fsUsage failed, shell result[${sshResult}]", e)
      }
    } finally {
      if (session != null) {
        session.close()
      }
    }

  }

  private val fsUsageStatsColMap: java.util.Map[String, Int] = new util.HashMap()

  private def mapCpuPcStatsCols(lineItems: Array[String]): Unit = {
    for (i <- 0 until lineItems.length) {
      val s = lineItems(i)
      if (s.contains("File") || s.contains("file") || s.contains("FS")) {
        fsUsageStatsColMap.put("fsName", i)
      } else if (s.contains("Mount") || s.contains("mount")) {
        fsUsageStatsColMap.put("moutDir", i)
      } else if (s.contains("Use") || s.contains("use") || s.contains("us")) {
        fsUsageStatsColMap.put("usedKb", i)
      } else if (s.contains("Ava") || s.contains("ava") || s.contains("Free") || s.contains("free")) {
        fsUsageStatsColMap.put("freeKb", i)
      } else if (s.contains("Cap") || s.contains("cap") || s.contains("Perc") || s.contains("perc")) {
        fsUsageStatsColMap.put("freePerc", i)
      }
    }
  }
}
