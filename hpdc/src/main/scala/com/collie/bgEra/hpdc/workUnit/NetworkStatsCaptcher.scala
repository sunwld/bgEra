package com.collie.bgEra.hpdc.workUnit

import java.util

import com.collie.bgEra.cloudApp.dtsf.{ResourceManager, WorkUnitRunable}
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo
import com.collie.bgEra.cloudApp.ssh2Pool.{Ssh2Session, SshResult}
import com.collie.bgEra.commons.util.SerialNumberUtils
import com.collie.bgEra.hpdc.workUnit.bean.MemStats
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Component

import scala.util.control.Breaks.{break, breakable}

@Component("networkStatsCaptcher")
class NetworkStatsCaptcher extends WorkUnitRunable{
  private val TOPIC = "hpdc-cpusum"
  private val SHELL = "NET"
  private val logger: Logger = LoggerFactory.getLogger("hpdc")

  @Autowired
  @Qualifier("hostShellMap")
  private val shellMap: java.util.Map[String,String] = null

  @Autowired
  private val kfkProducer: KafkaProducer[String, Object] = null

  @Autowired
  private val resManager: ResourceManager = null

  override def runWork(unit: WorkUnitInfo): Unit = {
    /**
      * LINUX:
      * OSTYPE:LINUX
      * Kernel Interface table
      * Iface      MTU    RX-OK RX-ERR RX-DRP RX-OVR    TX-OK TX-ERR TX-DRP TX-OVR Flg
      * eth0      1500 324014117      0      0 0      296716966      0      0      0 BMRU
      * lo       65536 67936113      0      0 0      67936113      0      0      0 LRU
      * virbr0    1500        0      0      0 0             0      0      0      0 BMU
      *
      */

    /**
      * AIX:
      * OSTYPE:AIX
      * Name  Mtu   Network     Address            Ipkts Ierrs    Opkts Oerrs  Coll
      * en4   1500  link#2      e4.1f.13.50.8c.a  2619372     0  1890990     3     0
      * en4   1500  192.168.253 192.168.253.10    2619372     0  1890990     3     0
      * en10  1500  link#3      0.14.5e.79.e7.fa 233884934     0 2501197800    74     0
      * en10  1500  133.96.17   133.96.17.10     233884934     0 2501197800    74     0
      * en10  1500  133.96.17   133.96.17.11     233884934     0 2501197800    74     0
      * en11  1500  link#4      0.14.5e.79.f7.bc 2226504857     0 4083421929     2     0
      * en11  1500  172.16.253  172.16.253.10    2226504857     0 4083421929     2     0
      * lo0   16896 link#1                       4087428230     0 4088067486     0     0
      * lo0   16896 127         127.0.0.1        4087428230     0 4088067486     0     0
      * lo0   16896 ::1%1                        4087428230     0 4088067486     0     0
      */


    /**
      * SOLARIS:
      * OSTYPE:SOLARIS
      * Name  Mtu  Net/Dest      Address        Ipkts  Ierrs Opkts  Oerrs Collis Queue
      * lo0   8232 127.0.0.0     127.0.0.1      1141313777 0     1141313777 0     0      0
      * ipmp0 1500 133.96.41.128 133.96.41.210  3093445555 0     2512392703 0     0      0
      * ipmp1 1500 10.0.0.0      10.10.10.8     3078800129 0     1330849528 0     0      0
      * net0  1500 0.0.0.0       0.0.0.0        186275027159 0     27372314605 0     0      0
      * net1  1500 0.0.0.0       0.0.0.0        157648436317 0     55272863669 0     0      0
      * net2  1500 0.0.0.0       0.0.0.0        930541224 0     81090807362 0     0      0
      * net3  1500 0.0.0.0       0.0.0.0        1092897 0     40386498882 0     0      0
      *
      * Name  Mtu  Net/Dest                    Address                     Ipkts  Ierrs Opkts  Oerrs Collis
      * lo0   8252 ::1                         ::1                         1141313777 0     1141313777 0     0
      * ipmp0 1500 default                     ::                          3093445560 0     2512392712 0     0
      * ipmp1 1500 default                     ::                          3078800129 0     1330849528 0     0
      * net0  1500 default                     ::                          186275027163 0     27372314605 0     0
      * net1  1500 default                     ::                          157648436317 0     55272863669 0     0
      * net2  1500 default                     ::                          930541224 0     81090807370 0     0
      * net3  1500 default                     ::                          1092897 0     40386498882 0     0
      */

    /**
      * OSTYPE:LINUX
      * Kernel Interface table
      * Iface       MTU Met    RX-OK RX-ERR RX-DRP RX-OVR    TX-OK TX-ERR TX-DRP TX-OVR Flg
      * eth1       1500   0 9666598192      0      0      0 10041194327      0      0      0 BMRU
      * lo        16436   0  4595405      0      0      0  4595405      0      0      0 LRU
      */
  }
}
