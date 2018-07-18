package com.collie.bgEra.cloudApp.bpq

import java.sql.SQLException
import java.util

import com.collie.bgEra.cloudApp.CloudAppContext
import com.collie.bgEra.cloudApp.utils.ContextHolder
import org.apache.ibatis.session.{SqlSession, SqlSessionFactory}
import org.quartz.{DisallowConcurrentExecution, Job, JobExecutionContext}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._

@DisallowConcurrentExecution
class BpqSqlQueueBus extends Job{
  private val logger: Logger = LoggerFactory.getLogger("bpq")
  private val appContext: CloudAppContext = ContextHolder.getBean(classOf[CloudAppContext])

  override def execute(context: JobExecutionContext): Unit = {
    var sqlStatement: String = null
    var params: Any = null
    var session: SqlSession = null
    var queueItem: QueueItem = null

    val jobDataMap = context.getJobDetail().getJobDataMap()
    val manager = jobDataMap.get("manager").asInstanceOf[BpqQueueManger]
    var modifyCount = 0
    var currSqlItem: SqlItem = null
    var factory:SqlSessionFactory = null

    try{
      val size = manager.getQueueSize()
      var i:Long = -1
      for(i <- Range(0,size.toInt)){
        modifyCount = 0
        val beginTS = System.currentTimeMillis()
        val popItemTS = System.currentTimeMillis()
        queueItem = manager.popItemFromQueue()
        logger.debug(s"pop record from queue elaspe: ${System.currentTimeMillis() - popItemTS} ms .")
        factory = appContext.getSqlSessionFactory(queueItem.factoryName)
        session = factory.openSession(false)
        try {
          val processSqlTS = System.currentTimeMillis()
          queueItem.sqlItems.foreach(sqlItem => {
            currSqlItem = sqlItem
            modifyCount = sqlItem.sqlType match {
              case SqlItem.DELETE_SQL => session.delete(sqlItem.sqlStatement, sqlItem.params)
              case SqlItem.INSERT_SQL => session.insert(sqlItem.sqlStatement, sqlItem.params)
              case SqlItem.UPDATE_SQL => session.update(sqlItem.sqlStatement, sqlItem.params)
              case _ => throw new IllegalArgumentException("sqlType must be QueueItem.DELETE_SQL or QueueItem.INSERT_SQL or QueueItem.UPDATE_SQL")
            }
          })
          queueItem.result.finish = true
          queueItem.result.modifyCount = modifyCount
          queueItem.result.success = true
          session.commit()
          logger.info(s"==============${System.currentTimeMillis() - beginTS} ms")
          logger.debug(s"process queueitem records elaspe: ${System.currentTimeMillis() - processSqlTS} ms .")
        } catch {
          case e: Exception => {
            logger.error(s"Exeption occured! when execute sql item:${currSqlItem}",e)
            e match {
              case _: IllegalArgumentException => queueItem.result.finish = true
              case oe: Exception => {
                oe.getCause match {
                  case _: SQLException => queueItem.result.finish = true
                  case _: Exception => {
                    manager.unshiftItemToQueue(queueItem)
                    return
                  }
                }
              }
            }
            session.rollback()
          }
        } finally {
          if(session != null){
            session.close()
            session = null
          }
        }
      }
    }catch {
      case e:Exception => logger.error("BpqSqlQueueBus failed!",e)
    }finally {

    }
  }
}
