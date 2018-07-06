package com.collie.bgEra.cloudApp.dtsf.impl

import com.collie.bgEra.cloudApp.dtsf.TaskManager
import com.collie.bgEra.cloudApp.dtsf.bean.{TaskInfo, TaskResult, WorkUnitResult}
import com.collie.bgEra.cloudApp.dtsf.mapper.TaskMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch

import scala.collection.JavaConversions._
import scala.collection.mutable

@Component
class TaskManagerImpl extends TaskManager{
  @Autowired
  val taskMapper: TaskMapper = null

  override def getPreparedTaskList(zkSessionId: String): mutable.Seq[mutable.Seq[String]] = {
    //get targetids
    val shardTargetMapper: mutable.Map[String,java.util.List[String]]
      = taskMapper.getTargetShardingMap()

    val myShardTargetList:mutable.Seq[String] = shardTargetMapper(zkSessionId)

    val zsetItemList = taskMapper.qryPerpredTaskListByTargets(zkSessionId,myShardTargetList,System.currentTimeMillis())
    if(zsetItemList != null && zsetItemList.size() > 0){
      val myShardTaskMap : mutable.Map[String,TaskInfo] =
        taskMapper.qryTaskInfoListByTargets(zkSessionId,myShardTargetList)
    }


    //
    null
  }

  override def runTask(taskInfo: StopWatch.TaskInfo): TaskResult = ???

  override def invokWorkUnit(workUnitName: String): WorkUnitResult = ???

  override def finishTask(taskResult: TaskResult, taskScheBeanNm: String): Unit = ???
}
