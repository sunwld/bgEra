package com.collie.bgEra.hpdc.workUnit

import java.util

import com.collie.bgEra.cloudApp.dtsf.{ResourceManager, WorkUnitRunable}
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo
import com.collie.bgEra.cloudApp.ssh2Pool.{Ssh2Session, SshResult}
import com.collie.bgEra.commons.util.SerialNumberUtils
import com.collie.bgEra.hpdc.service.StatisticsCalculateIncacheService
import com.collie.bgEra.hpdc.service.bean.CalculateIncacheStatsValue
import com.collie.bgEra.hpdc.workUnit.bean.{HostNetStats, NetworkErrorsStats}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class NetworkErrorsStatsCaptcher extends WorkUnitRunable {
  private val TOPIC = "hpdc-neterrors"
  private val SHELL = "NETWORK_ERRORS"
  private val logger: Logger = LoggerFactory.getLogger("hpdc")

  @Autowired
  @Qualifier("hostShellMap")
  private val shellMap: java.util.Map[String, String] = null

  @Autowired
  private val kfkProducer: KafkaProducer[String, NetworkErrorsStats] = null

  @Autowired
  private val resManager: ResourceManager = null

  @Autowired
  private val statisticsCalculateIncacheService: StatisticsCalculateIncacheService[String] = null

  override def runWork(workUnitInfo: WorkUnitInfo): Unit = {
    /*
      * linux、aix关键字 error fail drop timeout bad
      * solaris 关键字 Err Error Fail Drop Overflow Timeout
      */

    val cmd = shellMap.get(SHELL)
    var session: Ssh2Session = null
    var sshResult: SshResult = null
    try {
      session = resManager.getHostSshConnPoolResource(workUnitInfo.getTargetId())
      sshResult = session.execCommand(cmd)

      //      (statid,diffSeconds,diffVal)
      val networkErrorsStats: NetworkErrorsStats = new NetworkErrorsStats()
      networkErrorsStats.snapId = SerialNumberUtils.getSerialByTrunc10s(workUnitInfo.thisTime, true)
      networkErrorsStats.targetId = workUnitInfo.targetId
      if (sshResult.isFinishAndCmdSuccess()) {

        var statsList: ListBuffer[(String, Double)] = null
        val osType = sshResult.getStrout().get(0).split(":")(1)
        osType match {
          case "LINUX" => statsList = parseLinuxResult(sshResult.getStrout())
          case "AIX" => statsList = parseAixResult(sshResult.getStrout())
          case "SOLARIS" => statsList = parseSolarisResult(sshResult.getStrout())
        }
        // map(statindex,(snapid, snapvalue))
        val statsMap: mutable.Map[String, (String, Double)] = mutable.HashMap[String, (String, Double)]()
        statsList.foreach(i => {
          statsMap.put(i._1, (networkErrorsStats.snapId, i._2))
        })
        val calculateIncacheStatsValue: CalculateIncacheStatsValue[String] = new CalculateIncacheStatsValue(networkErrorsStats.snapId, statsMap)
        networkErrorsStats.statsResult = statisticsCalculateIncacheService.calculateDiff2LastValue(SHELL, workUnitInfo.targetId, calculateIncacheStatsValue)
        val record: ProducerRecord[String, NetworkErrorsStats] = new ProducerRecord(TOPIC, workUnitInfo.targetId, networkErrorsStats)
        kfkProducer.send(record)
      }
    } catch {
      case e: Exception => {
        logger.warn(s"Captcher NetworkErrorsStats failed, shell result[${sshResult}]", e)
      }
    } finally {
      if (session != null) {
        session.close()
      }
    }
  }

  private def parseLinuxResult(resLines: util.List[String]): ListBuffer[(String, Double)] = {
    /*
       Ip:
           374800206 total packets received
           0 forwarded
           0 incoming packets discarded
            ... ...
           OutOctets: 59599425660
           InMcastOctets: 106284
           OutMcastOctets: 5746
           InBcastOctets: 838368
           InNoECTPkts: 393945662
         */
    /**
      * linux、aix关键字 error fail drop timeout bad
      * solaris 关键字 Err Error Fail Drop Overflow Timeout
      */

    val parseResult: ListBuffer[(String, Double)] = ListBuffer()

    val linuxDataParten = "^\\s+".r
    val keywordsParten = "([eE]rr)|([fF]ail)|([dD]rop)|([tT]imeout)|([bB]ad)".r
    val numHeadParten = "^\\d".r
    val numTailParten = "\\d$".r

    var statsTopic: String = null

    resLines.foreach(line => {
      if (!linuxDataParten.findFirstIn(line).isEmpty) {
        //   374800206 total packets received

        val trimedLine = line.trim()
        if (!keywordsParten.findFirstIn(trimedLine).isEmpty) {
          /*     6 outgoing packets dropped
                 10 dropped because of missing route
                 4 input ICMP message failed.
                 0 ICMP messages failed
                 81105 failed connection attempts
                 0 bad segments received.
                 0 packet receive errors
                 0 receive buffer errors
                 0 send buffer errors
                 68 timeouts after SACK recovery
                 328 other TCP timeouts
                 4 SACK retransmits failed
                 206 connections aborted due to timeout
                 TCPDeferAcceptDrop: 6
            */
          if (!numHeadParten.findFirstIn(trimedLine).isEmpty) {
            val splitedVals = trimedLine.split(" ", 2)
            parseResult.append((splitedVals(1), splitedVals(0).toDouble))

          } else if (numTailParten.findFirstIn(trimedLine).isEmpty) {
            val splitPos = trimedLine.lastIndexOf(" ")
            parseResult.append((trimedLine.substring(0, splitPos), trimedLine.substring(splitPos + 1).toDouble))
          }
        }
      } else {
        //Ip:
        //
        if (line == null || "".equals(line)) {} else {
          statsTopic = line.trim().replace(":", "")
        }
      }
    })
    parseResult
  }

  private def parseAixResult(resLines: util.List[String]): ListBuffer[(String, Double)] = {
    /*
    OSTYPE:AIX
    icmp:
            4069305 calls to icmp_error
            0 errors not generated because old message was icmp
                    prefix advertisements: 0
                    bad prefix advertisements: 0
            0 message responses generated
      */
    /**
      * linux、aix关键字 error fail drop timeout bad
      * solaris 关键字 Err Error Fail Drop Overflow Timeout
      */

    val parseResult: ListBuffer[(String, Double)] = ListBuffer()

    val dataParten = "^\\s+".r
    val keywordsParten = "([eE]rr)|([fF]ail)|([dD]rop)|([tT]imeout)|([bB]ad)".r
    val numHeadParten = "^\\d".r
    val numTailParten = "\\d$".r

    var statsTopic: String = null

    resLines.foreach(line => {
      if (!dataParten.findFirstIn(line).isEmpty) {
        //   4069305 calls to icmp_error

        val trimedLine = line.trim()
        if (!keywordsParten.findFirstIn(trimedLine).isEmpty) {
          /*  4102667 calls to icmp_error
              4102667 calls to icmp_error
              0 errors not generated because old message was icmp
              0 messages with bad code fields
              0 bad checksums
              0 messages with bad length
              0 messages received with bad checksum
                      0 packets with bad hardware assisted checksum
                      9 discarded for bad checksums
                      0 discarded for bad header offset fields
              17488392 connections closed (including 114185 drops)
              69675 embryonic connections dropped
              387089 retransmit timeouts
                      158 connections dropped by rexmit timeout
              143 persist timeouts
                      0 connections dropped due to persist timeout
              2372432 keepalive timeouts
                      9310 connections dropped by keepalive
              72 packets dropped due to memory allocation failure
              0 spliced connections timeout
              0 spliced connections persist timeout
              0 spliced connections keepalive timeout
              2661 Connections dropped due to bad ACKs
              0 bad data length fields
              0 bad checksums
              4103256 dropped due to no socket
              2594047 broadcast/multicast datagrams dropped due to no socket
              0 bad header checksums
              0 with bad options
              0 fragments dropped (dup or out of space)
              2707 fragments dropped after timeout
              0 output packets dropped due to no bufs, etc.
              3856549 IP Multicast packets dropped due to no receiver
              0 path MTU discovery response timeouts
              0 path MTU discovery memory allocation failures
              0 packets dropped by threads
              0 packets dropped due to the full socket receive buffer
              0 dead gateway detection packet allocation failures
              0 dead gateway detection gateway allocation failures
              0 incoming packets dropped due to MLS filters
              0 fragments dropped (dup or out of space)
              0 fragments dropped after timeout
              0 output packets dropped due to no bufs, etc.
              0 packets dropped due to the full socket receive buffer
              0 packets not delivered due to bad raw IPv6 checksum
              0 incoming packets dropped due to MLS filters
              589 calls to icmp6_error
              0 errors not generated because old message was icmpv6
              0 messages with bad code fields
              0 bad checksums
              0 messages with bad length
                              bad group queries: 0
                              bad group reports: 0
                      bad group terminations: 0
                      bad router solicitations: 0
                      bad router advertisements: 0
                      bad neighbor solicitations: 0
                      bad neighbor advertisements: 0
                      bad redirects: 0
                      bad home agent address discovery requests: 0
                      bad home agent address discovery replies: 0
                      bad prefix solicitations: 0
                      bad prefix advertisements: 0
            */
          if (!numHeadParten.findFirstIn(trimedLine).isEmpty) {
            val splitedVals = trimedLine.split(" ", 2)
            parseResult.append((splitedVals(1), splitedVals(0).toDouble))

          } else if (numTailParten.findFirstIn(trimedLine).isEmpty) {
            val splitPos = trimedLine.lastIndexOf(" ")
            parseResult.append((trimedLine.substring(0, splitPos), trimedLine.substring(splitPos + 1).toDouble))
          }
        }
      } else {
        //Ip:
        //
        if (line == null || "".equals(line)) {} else {
          statsTopic = line.trim().replace(":", "")
        }
      }
    })
    parseResult
  }

  private def parseSolarisResult(resLines: util.List[String]): ListBuffer[(String, Double)] = {
    /*
    OSTYPE:SOLARIS

    RAWIP   rawipInDatagrams    =   278     rawipInErrors       =     0
        rawipInCksumErrs    =     0     rawipOutDatagrams   =    96
        rawipOutErrors      =     0
    UDP     udpInDatagrams      =104589051596       udpInErrors         =     0
        udpOutDatagrams     =95868811817        udpOutErrors        =     0
        tcpAttemptFails     =  2515     tcpEstabResets      = 25409
        tcpTimRetransDrop   =  3696     tcpTimKeepalive     =19169594
        tcpTimKeepaliveProbe=3684196    tcpTimKeepaliveDrop =  2922
        tcpListenDrop       =     0     tcpListenDropQ0     =     0
        tcpHalfOpenDrop     =     0     tcpOutSackRetrans   =277025
        ipInReceives        =1107716678 ipInHdrErrors       =     0
        ipInAddrErrors      =     0     ipInCksumErrs       =     2
        ipReasmTimeout      =    15     ipReasmReqds        =2345096121
        ipReasmOKs          =2353204601 ipReasmFails        =116011
        ipFragOKs           =1025449896 ipFragFails         =     0
        tcpInErrs           =     0     udpNoPorts          =32375631
        udpInCksumErrs      =  6543     udpInOverflows      =576416
        rawipInOverflows    =     0     ipsecInSucceeded    =     0
        ipsecInFailed       =     0     ipInIPv6            =     0
        ipv6InReceives      =207645606  ipv6InHdrErrors     =     0
        ipv6InTooBigErrors  =     0     ipv6InNoRoutes      =     0
        ipv6InAddrErrors    =     0     ipv6InUnknownProtos =     0
        ipv6OutFragFails    =     0     ipv6OutFragCreates  =     0
        ipv6ReasmFails      =     0     ipv6InMcastPkts     =     0
        udpInCksumErrs      =     0     udpInOverflows      =     0
        rawipInOverflows    =     0     ipv6InIPv4          =     0
    ICMPv4  icmpInMsgs          = 28137     icmpInErrors        =     0
        icmpInCksumErrs     =     0     icmpInUnknowns      =     5
        icmpOutDrops        =     0     icmpOutErrors       =     0
        icmpInOverflows     =  4873
    ICMPv6  icmp6InMsgs         = 24444     icmp6InErrors       =     0
        icmp6InOverflows    =     0
        icmp6OutMsgs        = 12222     icmp6OutErrors      =     0
        sctpOutOfBlue       =     0     sctpChecksumError   =     0
        sctpTimRetrans      =     0     sctpTimRetransDrop  =     0
        sctpTimHearBeatProbe=     0     sctpTimHearBeatDrop =     0
        sctpListenDrop      =     0     sctpInClosed        =     0
        sdpPrFails          =     0     sdpRejects          =     0
        */
    /**
      * linux、aix关键字 error fail drop timeout bad
      * solaris 关键字 Err Error Fail Drop Overflow Timeout
      */

    val parseResult: ListBuffer[(String, Double)] = ListBuffer()

    val dataParten = "(\\w+)\\s*=\\s*(\\d+)".r
    val keywordsParten = "([eE]rr)|([fF]ail)|([dD]rop)|([tT]imeout)|([Oo]verflow)".r
    val numHeadParten = "^\\d".r
    val numTailParten = "\\d$".r


    resLines.foreach(line => {
      if (!keywordsParten.findFirstIn(line).isEmpty) {
        //   4069305 calls to icmp_error

        dataParten.findAllMatchIn(line).foreach(m => {
          /*
            * ICMPv4  icmpInMsgs          = 28137     icmpInErrors        =     0
            *         icmpInCksumErrs     =     0     icmpInUnknowns      =     5
            */
          val k = m.group(1)
          if (!keywordsParten.findFirstIn(k).isEmpty) {
            val v = m.group(2).toDouble
            parseResult.append((k, v))
          }
        })
      }
    })
    parseResult
  }
}
