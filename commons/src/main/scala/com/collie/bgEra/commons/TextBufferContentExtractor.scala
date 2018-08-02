package com.collie.bgEra.commons

import java.text.{ParsePosition, SimpleDateFormat}
import java.util.Date

import com.collie.bgEra.commons.util.{ArrayUtils, StringUtils}
import org.apache.commons.beanutils.ConversionException

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex
import scala.util.control.Breaks._

class TextBufferContentExtractor private() {

  private var text: mutable.Buffer[String] = null
  private var datePatterns: Array[String] = null


  /**
    * text have title,data will find all data lines,then ext data by column
    * only theses samples can be used:
    * {
    * title1 title2 title3 title4
    * aaa    bbb    ccc    ddd
    * uuu    mmm    aaa    nnn
    * }
    * or
    * {
    * title1 title2 title3 title4
    * aaa    bbb    ccc    ddd
    *
    * title1 title2 title3 title4
    * uuu    mmm    aaa    nnn
    * }
    *
    * use extractDataByColumnName("&#94;title\\d+\\s+.*$".r,"&#94;(?!title)\\w+\\s+.*$".r,"\\s+".r,("title2".r,"title3".r))
    *
    * @param titleRegx
    * @param dataRegx
    * @param splitRegx
    * @param columnNamesRegex
    * @return
    */
  def extractDataByColumnName(titleRegx: Regex, footRegx: Regex, dataRegx: Regex, splitRegx: Regex, columnNamesRegex: mutable.Buffer[(String, Regex)]): mutable.Buffer[mutable.Map[String, String]] = {
    if (text == null || text.isEmpty) return ListBuffer[mutable.Map[String, String]]()

    val colPositionMap = mutable.HashMap[String, Int]()
    val extedResult = ListBuffer[mutable.Map[String, String]]()

    breakable {
      text.foreach(line => {

        line match {
          case dataRegx() => {
            val vals = splitRegx.split(line)
            val valMap = mutable.Map[String, String]()

            colPositionMap.foreach(valp => {
              valMap.put(valp._1, ArrayUtils.geti(vals, valp._2))
            })
            extedResult.append(valMap)
          }
          case titleRegx() => {
            val titles = splitRegx.split(line)

            for (i <- 0 until titles.length) {
              columnNamesRegex.foreach(col => {
                if (!col._2.findFirstIn(titles(i)).isEmpty) {
                  colPositionMap.put(col._1, i)
                }
              })
            }
          }
          case footRegx() => {
            break()
          }
          case _ => {}
        }
      })
    }


    extedResult
  }

  def extractConvertDataByColumnName(titleRegx: Regex, footRegx: Regex, dataRegx: Regex, splitRegx: Regex,
                                     columnNamesRegex: mutable.Buffer[(String, Regex, Int)]): mutable.Buffer[mutable.Map[String, Object]] = {
    if (text == null || text.isEmpty) return ListBuffer[mutable.Map[String, Object]]()

    val colPositionMap = mutable.HashMap[String, (Int, Int)]()
    val extedResult = ListBuffer[mutable.Map[String, Object]]()

    breakable {
      text.foreach(line => {

        line match {
          case dataRegx() => {
            val vals = splitRegx.split(line)
            val valMap = mutable.Map[String, Object]()

            colPositionMap.foreach(valp => {
              valMap.put(valp._1, convertData(ArrayUtils.geti(vals, valp._2._1), valp._2._2))
            })
            extedResult.append(valMap)
          }
          case titleRegx() => {
            val titles = splitRegx.split(line)

            for (i <- 0 until titles.length) {
              columnNamesRegex.foreach(col => {
                if (!col._2.findFirstIn(titles(i)).isEmpty) {
                  colPositionMap.put(col._1, (i, col._3))
                }
              })
            }
          }
          case footRegx() => {
            break()
          }
          case _ => {}
        }
      })
    }


    extedResult
  }

  def extractDataByRegexGroupName(dataLineGroupedRegex: mutable.Buffer[Regex], footRegx: Regex, groupNames: mutable.Buffer[String]): mutable.Buffer[mutable.Map[String, String]] = {
    if (text == null || text.isEmpty) return ListBuffer[mutable.Map[String, String]]()

    val extedResult = ListBuffer[mutable.Map[String, String]]()

    breakable {
      text.foreach(line => {
        dataLineGroupedRegex.foreach(gr => {
          val matches = gr.findAllMatchIn(line)
          matches.foreach(m => {
            val machedVal = mutable.Map[String, String]()
            groupNames.foreach(gn => {
              machedVal.put(gn, m.group(gn))
            })
            extedResult.append(machedVal)
          })
        })

        if (!footRegx.findFirstIn(line).isEmpty) {
          break()
        }
      })
    }

    extedResult
  }

  def extractConvertDataByRegexGroupName(dataLineGroupedRegex: mutable.Buffer[Regex], footRegx: Regex,
                                         groupNameTypes: mutable.Buffer[(String, Int)]): mutable.Buffer[mutable.Map[String, Object]] = {
    if (text == null || text.isEmpty) return ListBuffer[mutable.Map[String, Object]]()

    val extedResult = ListBuffer[mutable.Map[String, Object]]()

    breakable {
      text.foreach(line => {
        dataLineGroupedRegex.foreach(gr => {
          val matches = gr.findAllMatchIn(line)
          matches.foreach(m => {
            val machedVal = mutable.Map[String, Object]()
            groupNameTypes.foreach(gn => {
              machedVal.put(gn._1, convertData(m.group(gn._1), gn._2))
            })
            extedResult.append(machedVal)
          })
        })
        if (!footRegx.findFirstIn(line).isEmpty) {
          break()
        }
      })
    }

    extedResult
  }

  def convertData(str: String, toType: Int): Object = {
    toType match {
      case TextBufferContentExtractor.stringType => str
      case TextBufferContentExtractor.intType => StringUtils.toInt(str)
      case TextBufferContentExtractor.longType => StringUtils.toLong(str)
      case TextBufferContentExtractor.floatType => StringUtils.toFloat(str)
      case TextBufferContentExtractor.doubelType => StringUtils.toDouble(str)
      case TextBufferContentExtractor.dateType => {
        var firstEx: Exception = null
        var parsedDate: Date = null
        datePatterns.foreach(pattern => {
          try {
            val dataFormat: SimpleDateFormat = new SimpleDateFormat(pattern)
            val pos = new ParsePosition(0)
            parsedDate = dataFormat.parse(str, pos)
            if (pos.getErrorIndex() >= 0 || pos.getIndex() != str.length() || parsedDate == null) {
              val msg = "Error converting 'String' to 'Date'" + " using pattern '" + pattern + "'";
              throw new ConversionException(msg)
            }
            return parsedDate
          } catch {
            case ex: Exception => {
              if (firstEx == null) firstEx = ex
            }
          }
        })
        throw firstEx
      }
      case _ => str
    }
  }
}

object TextBufferContentExtractor {
  val defaultDatePatterns = Array[String]("yyyyMMddHHmmss", "yyyy-MM-dd", "yyyy/MM/dd", "yy-MM-dd", "dd/MM/yy", "yy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss")

  def apply(t: mutable.Buffer[String], datePatterns: Array[String] = defaultDatePatterns): TextBufferContentExtractor = {
    val textContentExtractor = new TextBufferContentExtractor()
    textContentExtractor.text = t
    textContentExtractor.datePatterns = datePatterns
    textContentExtractor
  }

  val stringType = 1
  val intType = 2
  val longType = 3
  val floatType = 4
  val doubelType = 5
  val dateType = 6
}

