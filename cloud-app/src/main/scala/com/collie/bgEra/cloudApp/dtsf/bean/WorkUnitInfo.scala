package com.collie.bgEra.cloudApp.dtsf.bean

/**
  * 单个任务单元，是任务链中的一个部分。
  * 任务单元信息中存放了任务的名称，相关任务目标的名称
  * 运行的顺序ID
  * 相关的资源MAP表
  * 以及WORKUNIT相关的SPRING BEAN的名称，这些WORKUNIT BEAN必须实现了WORKUNITRUNABLE接口
  */
class WorkUnitInfo {

  var workUnitName: String = _

  var targetId: String = _

  var runOrder: Int = _

  var resourceMap: Map[String, ResourceInfo] = _

  var springBeanName: String = _
}
