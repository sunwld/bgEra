package com.collie.bgEra.cloudApp.dtsf.bean

import java.util.Date

import scala.beans.BeanProperty

class TaskInfo {
  @BeanProperty var taskName: String = _

  @BeanProperty var targetId: String = _

  @BeanProperty var description: String = _

  @BeanProperty var workUnitList: java.util.List[String] = _

  @BeanProperty var taskSchedulerBean: String = _

  @BeanProperty var taskThreadPoolName: String = _

  @BeanProperty var status: String =_

  @BeanProperty var runResult: String = _

  @BeanProperty var errors: Long =_

  @BeanProperty var thisTime: Date =_

  @BeanProperty var nextTime: Date = _

  override def toString = s"TaskInfo(taskName=$taskName, targetId=$targetId, description=$description, workUnitList=$workUnitList, taskSchedulerBean=$taskSchedulerBean, taskThreadPoolName=$taskThreadPoolName, status=$status, runResult=$runResult, errors=$errors, thisTime=$thisTime, nextTime=$nextTime)"
}
