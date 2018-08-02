package com.collie.bgEra.hpdc.workUnit

import java.util

import com.collie.bgEra.cloudApp.dtsf.{ResourceManager, WorkUnitRunable}
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo
import com.collie.bgEra.cloudApp.ssh2Pool.{Ssh2Session, SshResult}
import com.collie.bgEra.commons.util.{ArrayUtils, SerialNumberUtils, StringUtils}
import com.collie.bgEra.hpdc.service.ShhShellMessgesService
import com.collie.bgEra.hpdc.service.bean.ShellInfo
import com.collie.bgEra.hpdc.workUnit.bean.SharedMemorySegStats
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Component

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import scala.collection.mutable

@Component("sharedMemorySegStatsCaptcher")
class SharedMemorySegStatsCaptcher extends WorkUnitRunable {
  private val TOPIC = "hpdc-sharedmem"
  private val SHELL = "SHARED_MEM.xsh"

  private val logger: Logger = LoggerFactory.getLogger("hpdc")
  @Autowired
  private val shhShellMessgesService: ShhShellMessgesService = null

  override def runWork(workUnitInfo: WorkUnitInfo): Unit = {

    var sharedMemorySegStats: SharedMemorySegStats = new SharedMemorySegStats()
    sharedMemorySegStats.snapId = SerialNumberUtils.getSerialByTrunc10s(workUnitInfo.thisTime, true)
    sharedMemorySegStats.targetId = workUnitInfo.targetId

    try {
      val sshResult = shhShellMessgesService.loadShellResults(new ShellInfo(SHELL, workUnitInfo.targetId, mutable.Map()))
      if (sshResult == null || sshResult.isEmpty) {
        return
      }

      //OSTYPE:LINUX
      //(shmid|ID,key|KEY,owner|OWNER,bytes|SEGSZ,cpid|CPID,lpid|LPID,attached|ATIME,detached|DTIME,changed|CTIME)
      val statResults = ListBuffer[(Long, String, String, Long, Long, Long, String, String, String)]()
      val osType = sshResult.get(0).split(":")(1)

      val parseResult: ListBuffer[(Long, String, String, Long, Long, Long, String, String, String, String)] = osType match {
        case "LINUX" => parseLinuxResult(sshResult)
        case ost if ("AIX".equals(ost) || "SOLARIS".equals(ost)) => parseOtherResult(sshResult)
        case _ => null
      }

      val javaList = new util.ArrayList[(Long, String, String, Long, Long, Long, String, String, String, String)]()
      parseResult.foreach(javaList.append(_))

      sharedMemorySegStats.statsResult = javaList

      logger.debug(s"sharedMemorySegStatsCaptcher:${sharedMemorySegStats.toString()}")

      val record: ProducerRecord[String, SharedMemorySegStats] = new ProducerRecord(TOPIC, workUnitInfo.targetId, sharedMemorySegStats)
      shhShellMessgesService.sendRecord2Kafka(record, classOf[SharedMemorySegStats])

    } catch {
      case e: Exception => {
        logger.warn(s"Captcher ShredMemorySegStats failed.", e)
      }
    }


  }

  private def parseOtherResult(lines: java.util.List[String]): ListBuffer[(Long, String, String, Long, Long, Long, String, String, String, String)] = {
    /**
      * OSTYPE:AIX
      * IPC status from /dev/mem as of Fri Jul 27 09:07:44 BEIST 2018
      * T        ID     KEY        MODE       OWNER    GROUP  CREATOR   CGROUP NATTCH     SEGSZ  CPID  LPID   ATIME    DTIME    CTIME
      * Shared Memory:
      * m         0 0x5500517c --rw-r--r--     root   system     root   system      1    124064 2491096 48955546 23:57:02  9:52:00 23:57:02
      * m   1048578 0x78000042 --rw-rw-rw-     root   system     root   system      1  33554432 2162754 60621790 23:59:07  8:58:20 23:59:07
      * m   1048579 0x78000041 --rw-rw-rw-     root   system     root   system      1  33554432 2162754 60621790 23:59:07  8:58:20 23:59:07
      * m 160432132 0xfa00250a --rw-rw----     root      dba     root      dba      3  30435368 64029360 64029360 23:34:55 23:35:12 16:06:11
      * m 334495749 0xfa002507 --rw-rw----     root      dba     root      dba      3  30435368 64029360 22609934 23:34:55 23:35:12 16:06:11
      * m   1048584 0x0000cace --rw-rw-rw-     root   system     root   system      0         2 2360176 2360176 20:00:16 20:00:16  0:00:53
      * m 100663306 0x95ad1b88 --rw-r-----   oracle      dba   oracle      dba    525 68719611904 7274958 22741780  9:07:44  9:07:44  0:25:12
      */
    /**
      * OSTYPE:SOLARIS
      * IPC status from <running system> as of Friday, July 27, 2018 09:07:53 AM CST
      * T         ID      KEY        MODE        OWNER    GROUP  CREATOR   CGROUP NATTCH      SEGSZ   CPID  LPID   ATIME    DTIME    CTIME
      * Shared Memory:
      * m  486539323   0x70425e70 --rw-r-----   oracle asmadmin   oracle asmadmin   1177      16384 23161  8494  9:07:28  9:07:52  1:54:43
      * m  486539322   0x0        --rw-r-----   oracle asmadmin   oracle asmadmin   1177 106837311488 23161  8494  9:07:28  9:07:52  1:54:43
      * m  486539321   0x0        --rw-r-----   oracle asmadmin   oracle asmadmin   1177  536870912 23161  8494  9:07:28  9:07:52  1:54:43
      * m 1795162165   0x58371e3c --rw-r-----     grid oinstall     grid oinstall     31      16384 21518 26045  8:20:58  8:20:59  1:54:04
      * m  301989940   0x0        --rw-r-----     grid oinstall     grid oinstall     31 1132462080 21518 26045  8:20:58  8:20:59  1:54:04
      * m  301989939   0x0        --rw-r-----     grid oinstall     grid oinstall     31    6963200 21518 26045  8:20:58  8:20:59  1:54:04
      */
    val resultList: ListBuffer[(Long, String, String, Long, Long, Long, String, String, String, String)] = ListBuffer()

    var segIdP, segKeyP, segOwnerP, segSizeP, segCpidP, segLpidP, segAtimeP, segCtimeP, segDtimeP, segModeP = -1

    val titleRegx = "(KEY\\s+\\w*\\s*\\OWNER)|(key\\s+\\w*\\s*\\owner)".r
    val dataRegx = "\\s+\\d+\\s+".r
    val dataSplit = "\\s+".r

    lines.foreach(line => {

      val lineItems = dataSplit.split(line)
      line match {
        case dataRegx() => {
          //(Long, String, String, Long, Long, Long, String, String, String, String)
          //(shmid|ID,key|KEY,owner|OWNER,bytes|SEGSZ,cpid|CPID,lpid|LPID,attached|ATIME,detached|DTIME,changed|CTIME,status|MODE)
          val segId = StringUtils.toLong(ArrayUtils.geti(lineItems, segIdP))
          val segKey = ArrayUtils.geti(lineItems, segKeyP)
          val segOwner = ArrayUtils.geti(lineItems, segOwnerP)
          val segSize = StringUtils.toLong(ArrayUtils.geti(lineItems, segSizeP))
          val segCpid = StringUtils.toLong(ArrayUtils.geti(lineItems, segCpidP))
          val segLpid = StringUtils.toLong(ArrayUtils.geti(lineItems, segLpidP))
          val segAtime = ArrayUtils.geti(lineItems, segAtimeP)
          val segCtime = ArrayUtils.geti(lineItems, segCtimeP)
          val segDtime = ArrayUtils.geti(lineItems, segDtimeP)
          val segMode = ArrayUtils.geti(lineItems, segModeP)

          resultList.append((segId, segKey, segOwner, segSize, segCpid, segLpid, segAtime, segDtime, segCtime, segMode))
        }
        case titleRegx() => {
          for (i <- 0 until lineItems.length) {
            lineItems(i) match {
              case s if ("ID".equals(s) || "id".equals(s)) => segIdP = i
              case s if ("KEY".equals(s) || "key".equals(s)) => segKeyP = i
              case s if ("OWNER".equals(s) || "owner".equals(s)) => segOwnerP = i
              case s if ("SEGSZ".equals(s) || "bytes".equals(s) || "size".equals(s)) => segSizeP = i
              case s if ("MODE".equals(s) || "status".equals(s) || "STATUS".equals(s)) => segModeP = i
              case s if ("CPID".equals(s) || "cpid".equals(s)) => segCpidP = i
              case s if ("LPID".equals(s) || "lpid".equals(s)) => segLpidP = i
              case s if ("ATIME".equals(s) || "atime".equals(s)) => segAtimeP = i
              case s if ("DTIME".equals(s) || "dtime".equals(s)) => segDtimeP = i
              case s if ("CTIME".equals(s) || "ctime".equals(s)) => segCtimeP = i
              case _ => {}
            }
          }
        }
        case _ => {}
      }
    })

    resultList
  }

  private def parseLinuxResult(lines: java.util.List[String]): ListBuffer[(Long, String, String, Long, Long, Long, String, String, String, String)] = {
    /**
      * OSTYPE:LINUX
      *
      * ------ Shared Memory Segments --------
      * key        shmid      owner      perms      bytes      nattch     status
      * 0x0113432a 196608     root       600        1000       6
      *
      *
      * ------ Shared Memory Attach/Detach/Change Times --------
      * shmid      owner      attached             detached             changed
      * 196608     root       Jul 24 03:16:01      Not set              Jul 24 03:16:01
      *
      *
      * ------ Shared Memory Creator/Last-op PIDs --------
      * shmid      owner      cpid       lpid
      * 196608     root       30777      30777
      *
      */
    //ID,KEY,OWNER,SIZE,STATUS
    val shareSegStats = ListBuffer[(Long, String, String, Long, String)]()
    var segStatIdP = -1
    var segStatKeyP = -1
    var segStatOwnerP = -1
    var segStatSizeP = -1
    var segStatStatusP = -1
    //ID,ATTACHED,DETACHED,CHANGED
    val shareSegTime = ListBuffer[(Long, String, String, String)]()
    var timeStatIdP = -1
    var timeStatAttP = -1
    var timeStatDetP = -1
    var timeStatChdP = -1
    //ID,CPID,LPID
    val shareSegPid = ListBuffer[(Long, Long, Long)]()
    var pidStatIdP = -1
    var pidStatCpidP = -1
    var pidStatLpidP = -1

    val shareSegStatsTopicRegx = "^-*\\s*Shared Memory Segments\\s*-*$".r
    val shareSegStatsTitleRegx = "^[Kk][Ee][Yy]".r
    val shareSegTimeTopicRegx = "^-+\\s*Shared Memory Attach/Detach/Change Times\\s*-+$".r
    val shareSegTimeTitleRegx = "(attached)|(ATIME)|(TIME)".r
    val shareSegPidTopicRegx = "^-+\\s*Shared Memory Creator/Last-op PIDs\\s*-+$".r
    val shareSegPidTitleRegx = "(lpid)|(LPID)".r
    val dataRegx = "\\s+\\d+\\s+".r
    val dataSplit = "\\s+".r

    var statsTopic: String = null

    val topic: String = null
    lines.foreach(line => {

      /**
        * * key        shmid      owner      perms      bytes      nattch     status
        * * 0x0113432a 196608     root       600        1000       6
        */

      line match {
        case shareSegStatsTopicRegx() => statsTopic = "shareSegStats"
        case shareSegTimeTopicRegx() => statsTopic = "shareSegTime"
        case shareSegPidTopicRegx() => statsTopic = "shareSegPid"
        case _ => {}
      }

      val lineItems = dataSplit.split(line)

      statsTopic match {
        case "shareSegStats" => {
          if (!dataRegx.findFirstIn(line).isEmpty) {
            val segStatId = StringUtils.toLong(ArrayUtils.geti(lineItems, segStatIdP))
            val segStatKey = ArrayUtils.geti(lineItems, segStatKeyP)
            val segStatOwner = ArrayUtils.geti(lineItems, segStatOwnerP)
            val segStatSize = StringUtils.toLong(ArrayUtils.geti(lineItems, segStatSizeP))
            val segStatStatus = ArrayUtils.getiNE(lineItems, segStatStatusP)
            //(Long, String, String, Long, String): id,key,owner,size,status
            shareSegStats.append((segStatId, segStatKey, segStatOwner, segStatSize, segStatStatus))
          } else if (!shareSegStatsTitleRegx.findFirstIn(line).isEmpty) {
            //key        shmid      owner      perms      bytes      nattch     status
            for (i <- 0 until lineItems.length) {
              lineItems(i) match {
                case s if ("shmid".equals(s) || "ID".equals(s) || "id".equals(s)) => segStatIdP = i
                case s if ("key".equals(s) || "KEY".equals(s)) => segStatKeyP = i
                case s if ("owner".equals(s) || "OWNER".equals(s)) => segStatOwnerP = i
                case s if ("bytes".equals(s) || "SEGSZ".equals(s) || "size".equals(s)) => segStatSizeP = i
                case s if ("status".equals(s) || "MODE".equals(s) || "STATUS".equals(s)) => segStatStatusP = i
                case _ => {}
              }
            }
          }
        }
        case "shareSegTime" => {
          if (!dataRegx.findFirstIn(line).isEmpty) {
            val timeStatId = StringUtils.toLong(ArrayUtils.geti(lineItems, timeStatIdP))
            val timeStatAtt = ArrayUtils.geti(lineItems, timeStatAttP)
            val timeStatDet = ArrayUtils.geti(lineItems, timeStatDetP)
            val timeStatChd = ArrayUtils.geti(lineItems, timeStatChdP)
            //ID,ATTACHED,DETACHED,CHANGED (Long, String, String, String)
            shareSegTime.append((timeStatId, timeStatAtt, timeStatDet, timeStatChd))
          } else if (!shareSegTimeTitleRegx.findFirstIn(line).isEmpty) {
            //shmid      owner      attached             detached             changed
            for (i <- 0 until lineItems.length) {
              lineItems(i) match {
                case s if ("shmid".equals(s) || "ID".equals(s) || "id".equals(s)) => timeStatIdP = i
                case s if ("attached".equals(s) || "ATIME".equals(s)) => timeStatAttP = i
                case s if ("detached".equals(s) || "DTIME".equals(s)) => timeStatDetP = i
                case s if ("changed".equals(s) || "CTIME".equals(s)) => timeStatChdP = i
                case _ => {}
              }
            }
          }
        }
        case "shareSegPid" => {
          if (!dataRegx.findFirstIn(line).isEmpty) {
            val pidStatId = StringUtils.toLong(ArrayUtils.geti(lineItems, pidStatIdP))
            val pidStatCpid = StringUtils.toLong(ArrayUtils.geti(lineItems, pidStatCpidP))
            val pidStatLpid = StringUtils.toLong(ArrayUtils.geti(lineItems, pidStatLpidP))
            // //ID,CPID,LPID (Long, Long, Long)
            shareSegPid.append((pidStatId, pidStatCpid, pidStatLpid))
          } else if (!shareSegPidTitleRegx.findFirstIn(line).isEmpty) {
            //shmid      owner      cpid       lpid
            for (i <- 0 until lineItems.length) {
              lineItems(i) match {
                case s if ("shmid".equals(s) || "ID".equals(s) || "id".equals(s)) => pidStatIdP = i
                case s if ("cpid".equals(s) || "CPID".equals(s)) => pidStatCpidP = i
                case s if ("lpid".equals(s) || "LPID".equals(s)) => pidStatLpidP = i
                case _ =>
              }
            }
          }
        }
        case _ => {}
      }
    })

    //(Long, String, String, Long, Long, Long, String, String, String)
    //(shmid|ID,key|KEY,owner|OWNER,bytes|SEGSZ,cpid|CPID,lpid|LPID,attached|ATIME,detached|DTIME,changed|CTIME,status|MODE)
    val resultList: ListBuffer[(Long, String, String, Long, Long, Long, String, String, String, String)] = ListBuffer()
    shareSegStats.foreach(segStat => {
      shareSegTime.foreach(segTime => {
        if (segStat._1 == segTime._1) {
          shareSegPid.foreach(segPid => {
            if (segStat._1 == segPid._1) {
              resultList.append((segStat._1, segStat._2, segStat._3, segStat._4, segPid._2, segPid._3, segTime._2, segTime._3, segTime._4, segStat._5))
            }
          })
        }
      })
    })
    resultList
  }
}
