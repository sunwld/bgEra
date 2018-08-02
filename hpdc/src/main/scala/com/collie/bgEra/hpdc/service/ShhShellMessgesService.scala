package com.collie.bgEra.hpdc.service

import com.collie.bgEra.cloudApp.dtsf.ResourceManager
import com.collie.bgEra.cloudApp.ssh2Pool.{Ssh2Session, SshResult}
import com.collie.bgEra.commons.{Map_BeanConvertor, TextBufferContentExtractor}
import com.collie.bgEra.hpdc.context.KafkaProducerSource
import com.collie.bgEra.hpdc.service.bean.{ExecuteShellException, ShellInfo}
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Service

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.reflect.ClassTag
import scala.util.matching.Regex

@Service
class ShhShellMessgesService {

  private val logger: Logger = LoggerFactory.getLogger("hpdc")

  @Autowired
  @Qualifier("hostShellMap")
  private val shellMap: java.util.Map[String, String] = null

  @Autowired
  private val resManager: ResourceManager = null
  @Autowired
  private val kafkaProducerSource: KafkaProducerSource = null


  def loadShellResults(shell: ShellInfo): mutable.Buffer[String] = {
    val cmd = shellMap.get(shell.shellName)
    var session: Ssh2Session = null
    var sshResult: SshResult = null
    try {
      session = resManager.getHostSshConnPoolResource(shell.targetId)
      sshResult = session.execCommand(cmd)

      if (sshResult.isFinishAndCmdSuccess()) {
        sshResult.getStrout()
      } else {
        throw new ExecuteShellException(sshResult.toString, null)
      }
    } catch {
      case ex: Exception => {
        if (sshResult != null)
          throw new ExecuteShellException(sshResult.toString, ex)
        throw ex
      }
    } finally {
      if (session != null) {
        session.close()
      }
    }
  }

  def formatColumedMessages2Map(messagesList: mutable.Buffer[String], titleRegx: Regex,
                                footRegex: Regex, dataRegx: Regex, splitRegx: Regex,
                                columnNamesRegex: mutable.Buffer[(String, Regex)]): mutable.Buffer[mutable.Map[String, String]] = {
    val extractor = TextBufferContentExtractor(messagesList)
    extractor.extractDataByColumnName(titleRegx, footRegex, dataRegx, splitRegx, columnNamesRegex)
  }

  def formatConvertColumedMessages2Map(messagesList: mutable.Buffer[String], titleRegx: Regex,
                                       footRegex: Regex, dataRegx: Regex, splitRegx: Regex,
                                       columnNamesRegex: mutable.Buffer[(String, Regex, Int)], datePatterns: Array[String] = TextBufferContentExtractor.defaultDatePatterns): mutable.Buffer[mutable.Map[String, Object]] = {
    val extractor = TextBufferContentExtractor(messagesList, datePatterns)
    extractor.extractConvertDataByColumnName(titleRegx, footRegex, dataRegx, splitRegx, columnNamesRegex)
  }

  def formatConvertColumedMessages2Bean[T: ClassTag](messagesList: mutable.Buffer[String],
                                                     titleRegx: Regex, footRegex: Regex, dataRegx: Regex,
                                                     splitRegx: Regex, columnNamesRegex: mutable.Buffer[(String, Regex)],
                                                     datePatterns: Array[String] = TextBufferContentExtractor.defaultDatePatterns): mutable.Buffer[T] = {
    val formatedResult = formatColumedMessages2Map(messagesList, titleRegx, footRegex, dataRegx, splitRegx, columnNamesRegex)
    val resultList = ListBuffer[T]()
    formatedResult.foreach(res => {

      resultList.append(Map_BeanConvertor.convertToBean[T](res, datePatterns))
    })
    resultList
  }

  def formatRegexGroupedMessages2Map(messagesList: mutable.Buffer[String],
                                     dataLineGroupedRegex: mutable.Buffer[Regex],
                                     footRegex: Regex, groupNames: mutable.Buffer[String]): mutable.Buffer[mutable.Map[String, String]] = {
    val extractor = TextBufferContentExtractor(messagesList)
    extractor.extractDataByRegexGroupName(dataLineGroupedRegex, footRegex, groupNames)
  }

  def formatConvertRegexGroupedMessages2Map(messagesList: mutable.Buffer[String],
                                            dataLineGroupedRegex: mutable.Buffer[Regex],
                                            footRegex: Regex, groupNames: mutable.Buffer[(String, Int)],
                                            datePatterns: Array[String] = TextBufferContentExtractor.defaultDatePatterns): mutable.Buffer[mutable.Map[String, Object]] = {
    val extractor = TextBufferContentExtractor(messagesList, datePatterns)
    extractor.extractConvertDataByRegexGroupName(dataLineGroupedRegex, footRegex, groupNames)
  }

  def formatConvertRegexGroupedMessages2Bean[T: ClassTag](messagesList: mutable.Buffer[String],
                                                          dataLineGroupedRegex: mutable.Buffer[Regex],
                                                          footRegex: Regex, groupNames: mutable.Buffer[String],
                                                          datePatterns: Array[String] = TextBufferContentExtractor.defaultDatePatterns): mutable.Buffer[T] = {
    val extractor = TextBufferContentExtractor(messagesList, datePatterns)
    val formatedResult = extractor.extractDataByRegexGroupName(dataLineGroupedRegex, footRegex, groupNames)
    val resultList = ListBuffer[T]()
    formatedResult.foreach(res => {
      resultList.append(Map_BeanConvertor.convertToBean[T](res, datePatterns))
    })
    resultList
  }

  def sendRecord2Kafka[T](record: ProducerRecord[String, T], clazz: Class[T]): Unit = {
    kafkaProducerSource.getKfkProducerSource(clazz).send(record)
  }


}

