package com.collie.bgEra.commons

import java.io.{BufferedReader, InputStream, InputStreamReader}

import com.collie.bgEra.commons.util.ArrayUtils

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex

class TextStreamContentExtractor private() {

  private var textStream: InputStream = null

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
  def extractDataByColumnName(titleRegx: Regex, dataRegx: Regex, splitRegx: Regex, columnNamesRegex: ListBuffer[(String, Regex)]): ListBuffer[mutable.Map[String, String]] = {
    if (textStream == null) return ListBuffer[mutable.Map[String, String]]()

    val textReader = new InputStreamReader(textStream)
    val textBufferReader = new BufferedReader(textReader)

    try {
      val colPositionMap = mutable.HashMap[String, Int]()
      val extedResult = ListBuffer[mutable.Map[String, String]]()

      var line: String = null
      val readLine = () => {
        line = textBufferReader.readLine()
        if (line == null) {
          false
        } else {
          true
        }
      }
      while (readLine()) {
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
          case _ => {}
        }
      }

      extedResult
    } finally {
      if (textBufferReader != null) {
        textBufferReader.close()
      }
      if (textReader != null) {
        textReader.close()
      }
      if (textStream != null) {
        textStream.close()
      }
    }
  }

  def extractDataByRegexGroupName(dataLineGroupedRegex: ListBuffer[Regex], groupNames: ListBuffer[String]): ListBuffer[mutable.Map[String, String]] = {
    if (textStream == null) return ListBuffer[mutable.Map[String, String]]()

    val textReader = new InputStreamReader(textStream)
    val textBufferReader = new BufferedReader(textReader)

    val extedResult = ListBuffer[mutable.Map[String, String]]()

    try {
      var line: String = null
      val readLine = () => {
        line = textBufferReader.readLine()
        if (line == null) {
          false
        } else {
          true
        }
      }

      while (readLine()) {
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
      }
    } finally {
      if (textBufferReader != null) {
        textBufferReader.close()
      }
      if (textReader != null) {
        textReader.close()
      }
      if (textStream != null) {
        textStream.close()
      }
    }
    extedResult
  }

}

object TextStreamContentExtractor {
  def apply(text: InputStream): TextStreamContentExtractor = {
    val textContentExtractor = new TextStreamContentExtractor()
    textContentExtractor.textStream = text
    textContentExtractor
  }
}

