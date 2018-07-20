package com.collie.bgEra.hpdc.workUnit

import java.util

import com.collie.bgEra.cloudApp.dtsf.{ResourceManager, WorkUnitRunable}
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo
import com.collie.bgEra.cloudApp.ssh2Pool.{SshResult, SshSession}
import org.apache.commons.pool2.impl.GenericObjectPool
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Component

@Component("cpuStatsCaptcher")
class CpuStatsCaptcher extends WorkUnitRunable{
  @Autowired
  @Qualifier("hostShellMap")
  private val shellMap: java.util.HashMap[String,String] = null

  @Autowired
  private val resManager: ResourceManager = null

  override def runWork(unit: WorkUnitInfo): Unit = {
    val cmd = shellMap.get("CPU.xsh")
    val sshSessionPool: GenericObjectPool[SshSession] = resManager.getHostSshConnPoolResource(unit.getTargetId())
    var session: SshSession = null
    var sshResult: SshResult = null
    val rs:java.util.Map[String,Any] = new util.HashMap()

    val keys = Array("user", "sys", "wait", "idle")
    try {
      session = sshSessionPool.borrowObject()
      sshResult = session.execCommand(cmd)
      if(sshResult.getExitCode() == null || sshResult.getExitCode() == 0) keys.foreach(k => {
//        val tmpStr: Array[String] = result.get(i).split("=")
//        if (tmpStr.length > 1) cfgBean.put(keys(i), tmpStr(1))
//        else cfgBean.put(keys(i), "")
      })
    } finally{
      if(session != null){
        if(sshSessionPool == null){
          session.closeSession()
        }else{
          sshSessionPool.returnObject(session)
        }
      }
    }

  }
}
