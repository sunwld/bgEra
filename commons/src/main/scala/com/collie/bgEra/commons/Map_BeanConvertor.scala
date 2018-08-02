package com.collie.bgEra.commons

import java.util.Date

import org.apache.commons.beanutils.converters.{DateConverter, LongConverter}
import org.apache.commons.beanutils.{BeanUtils, ConvertUtils, Converter}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._
import java.beans.{Introspector, PropertyDescriptor}

import scala.reflect.ClassTag
import scala.util.control.Breaks._

object Map_BeanConvertor {

  private val logger: Logger = LoggerFactory.getLogger("Map_BeanConvertor")

  def convertToBean[T](map: java.util.Map[String, String])(implicit m: ClassTag[T]): T = {
    val cls: Class[_] = m.runtimeClass
    val obj = cls.newInstance().asInstanceOf[T]
    BeanUtils.populate(obj, map)
    obj
  }

  def convertToBean[T](map: java.util.Map[String, String], dataFormatPatterns: Array[String])(implicit m: ClassTag[T]): T = {
    val cls: Class[_] = m.runtimeClass
    val obj = cls.newInstance().asInstanceOf[T]

    val dc = new DateConverter()
    dc.setPatterns(dataFormatPatterns)
    ConvertUtils.register(dc, classOf[Date])
    BeanUtils.populate(obj, map)
    obj
  }

  def convertToBean[T](map: java.util.Map[String, Object], valConverters: java.util.Map[Class[_], Converter])(implicit m: ClassTag[T]): T = {
    val cls: Class[_] = m.runtimeClass
    val obj = cls.newInstance().asInstanceOf[T]

    valConverters.foreach(vc => {
      ConvertUtils.register(vc._2, vc._1)
    })
    BeanUtils.populate(obj, map)
    obj
  }

  def convertToMap(obj: Object): java.util.Map[String, Object] = {
    val map: java.util.Map[String, Object] = new java.util.HashMap[String, Object]

    val beanInfo = Introspector.getBeanInfo(obj.getClass)
    val propertyDescriptors: Array[PropertyDescriptor] = beanInfo.getPropertyDescriptors

    propertyDescriptors.foreach(p => {
      breakable {
        val key = p.getName()
        if (key.compareToIgnoreCase("class") == 0) {
          break()
        }
        val getter = p.getReadMethod()
        if (getter != null) {
          map.put(key, getter.invoke(obj))
        }
      }
    })
    map
  }

}





