package com.collie.bgEra.flexwf.controllers

import org.activiti.engine.{RuntimeService, TaskService}
import org.activiti.engine.runtime.ProcessInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{RequestMapping, RequestParam, RestController}

@RestController(Array("/flexwf"))
class TestController {

  @Autowired
  private val runtimeService: RuntimeService = null
  @Autowired
  private val taskService:TaskService = null


  @RequestMapping(Array("startOneProcess"))
  def startOneProcess(@RequestParam("processId", required = false, defaultValue = "testWorkFlow") processId: String,
                      @RequestParam("workContent", required = false, defaultValue = "") workContent: String): Unit = {

    val workContent = new java.util.HashMap[String, Object]()
    val pi: ProcessInstance = runtimeService.startProcessInstanceByKey(processId, workContent)
    println(pi)

    s"SUCCESS${pi.getId()}"
  }

  def queryMyWorkTask(): Object = {


    null

  }

}
