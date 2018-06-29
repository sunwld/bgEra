package com.collie.bgEra.cloudApp.dtsf.bean

import scala.beans.BeanProperty

class TaskInfo {
  @BeanProperty var taskName: String = _

  @BeanProperty var targetId: String = _

  @BeanProperty var workUnitList: List[WorkUnitInfo] = _

  @BeanProperty var taskSchedulerBean: String = _

  @BeanProperty var taskThreadPoolName: String = _

}
