package com.collie.bgEra.cloudApp.dtsf.bean

import scala.beans.BeanProperty

class TaskInfo {

  @BeanProperty var targetId: String = _

  @BeanProperty var taskSeq: Int = _

  @BeanProperty var workUnitList: List[WorkUnitInfo] = _

  @BeanProperty var taskSchedulerBean: String = _


}
