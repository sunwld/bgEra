package com.collie.bgEra.hpdc

import java.util.Date

import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo
import com.collie.bgEra.hpdc.workUnit.{CpuProcessorStatsCaptcher, NetworkStatsCaptcher, SharedMemorySegStatsCaptcher}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{RequestMapping, RestController}

@RestController
@RequestMapping(Array("/hpdc"))
class TestController {
  private val logger: Logger = LoggerFactory.getLogger("opdc")

  @Autowired
  private val networkStatsCaptcher:NetworkStatsCaptcher = null

  @RequestMapping(Array("/networkStatsCaptcher"))
  def test = {

    val wu = new WorkUnitInfo()
    wu.targetId = "DEVOPS1"
    wu.thisTime = new Date()

    networkStatsCaptcher.runWork(wu)

    new Tuple2(1, "String")
  }

  @Autowired
  private val cpuProcessorStatsCaptcher:CpuProcessorStatsCaptcher = null

  @RequestMapping(Array("/cpuProcessorStatsCaptcher"))
  def test2 = {

    val wu = new WorkUnitInfo()
    wu.targetId = "DEVOPS4"
    wu.thisTime = new Date()

    cpuProcessorStatsCaptcher.runWork(wu)

    new Tuple2(1, "String")
  }

  @Autowired
  private val sharedMemorySegStatsCaptcher:SharedMemorySegStatsCaptcher = null

  @RequestMapping(Array("/sharedMemorySegStatsCaptcher"))
  def test3 = {

    val wu = new WorkUnitInfo()
    wu.targetId = "DEVOPS1"
    wu.thisTime = new Date()

    sharedMemorySegStatsCaptcher.runWork(wu)

    new Tuple2(1, "String")
  }
}