package com.collie.bgEra.cloudApp.kryoUtil

import org.objenesis.strategy.StdInstantiatorStrategy
import com.esotericsoftware.kryo.io.{Input, Output}
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.UnsupportedEncodingException
import java.text.SimpleDateFormat
import java.util.concurrent.ConcurrentHashMap
import java.{lang, util}

import com.esotericsoftware.kryo.Kryo
import org.apache.commons.codec.binary.Base64
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import java.util.{Date}


object KryoUtil {
  private val logger: Logger = LoggerFactory.getLogger("kryoUtil")
  private val DEFAULT_ENCODING = "UTF-8"
  //每个线程的 Kryo 实例//每个线程的 Kryo 实例

  private val kryoMoudleClassMap: util.Map[Integer, util.List[Class[_]]] = new util.TreeMap[Integer, util.List[Class[_]]]()

  def addMoudleClassList(moduleOrder: Integer, classList: util.List[Class[_]]): Unit = {
    kryoMoudleClassMap.synchronized {
      kryoMoudleClassMap.put(moduleOrder, classList)
    }
  }

  private val kryoCustomClassRegMap: util.Map[Integer, Class[_]] = new util.HashMap()


  def addCustomClassRegMap(customClassMap: util.Map[Integer, Class[_]]): Unit = {
    kryoCustomClassRegMap.synchronized {
      if (customClassMap != null) {
        customClassMap.foreach(x => {
          if (kryoCustomClassRegMap.containsKey(x._1)) {
            logger.warn(s"class register id [${x._1}] is allready exists, registed class[${kryoCustomClassRegMap.get(x._1)}], new class[${x._2}].")
          }
          kryoCustomClassRegMap.put(x._1, x._2)
        })
      }
    }
  }

  private val kryoLocal = new ThreadLocal[Kryo]() {
    override protected def initialValue: Kryo = {
      val kryo = new Kryo()

      /**
        * 不要轻易改变这里的配置！更改之后，序列化的格式就会发生变化，
        * 上线的同时就必须清除 Redis 里的所有缓存，
        * 否则那些缓存再回来反序列化的时候，就会报错
        */
      //支持对象循环引用（否则会栈溢出）
      kryo.setReferences(true) //默认值就是 true，添加此行的目的是为了提醒维护者，不要改变这个配置

      //不强制要求注册类（注册行为无法保证多个 JVM 内同一个类的注册编号相同；而且业务系统中大量的 Class 也难以一一注册）
      //kryo.setRegistrationRequired(false) //默认值就是 false，添加此行的目的是为了提醒维护者，不要改变这个配置

      //Fix the NPE bug when deserializing Collections.
      kryo.getInstantiatorStrategy.asInstanceOf[Kryo.DefaultInstantiatorStrategy].setFallbackInstantiatorStrategy(new StdInstantiatorStrategy)

      //Regist class map to kryo, sample classes begin 101 to 150, app classes > 150
      kryo.register(classOf[util.HashMap[_, _]], 101)
      kryo.register(classOf[util.ArrayList[_]], 102)
      kryo.register(classOf[ListBuffer[_]], 103)
      kryo.register(classOf[ArrayBuffer[_]], 114)
      kryo.register(classOf[mutable.Seq[_]], 104)
      kryo.register(classOf[mutable.Map[_, _]], 105)
      kryo.register(classOf[mutable.HashMap[_, _]], 106)
      kryo.register(classOf[util.Map[_, _]], 107)
      kryo.register(classOf[util.List[_]], 108)
      kryo.register(classOf[String], 109)
      kryo.register(classOf[Int], 110)
      kryo.register(classOf[Float], 111)
      kryo.register(classOf[Long], 112)
      kryo.register(classOf[Double], 113)
      //      kryo.register(classOf[Array[String]], 114)
      //      kryo.register(classOf[Array[Int]], 115)
      //      kryo.register(classOf[Array[Float]], 116)
      //      kryo.register(classOf[Array[Long]], 117)
      //      kryo.register(classOf[Array[Double]], 118)
      //      kryo.register(classOf[Array[Any]], 119)
      //      kryo.register(classOf[Array[Any]], 119)
      kryo.register(classOf[Array[_]], 120)
      //      kryo.register(classOf[Array[Byte]], 121)
      //      kryo.register(classOf[Array[Char]], 122)
      kryo.register(classOf[BigDecimal], 123)
      //      kryo.register(classOf[Array[lang.String]], 130)
      //      kryo.register(classOf[Array[lang.Integer]], 131)
      //      kryo.register(classOf[Array[lang.Float]], 132)
      //      kryo.register(classOf[Array[lang.Long]], 133)
      //      kryo.register(classOf[Array[lang.Double]], 134)
      //      kryo.register(classOf[Array[Object]], 135)
      //      kryo.register(classOf[Array[lang.Byte]], 136)
      //      kryo.register(classOf[Array[StringBuffer]], 137)
      kryo.register(classOf[Date], 138)
      kryo.register(classOf[java.math.BigDecimal], 139)
      kryo.register(classOf[util.Map[_, _]], 150)
      kryo.register(classOf[util.HashMap[_, _]], 140)
      kryo.register(classOf[util.ArrayList[_]], 141)
      kryo.register(classOf[util.List[_]], 151)
      kryo.register(classOf[util.LinkedList[_]], 142)
      kryo.register(classOf[util.HashSet[_]], 143)
      kryo.register(classOf[util.TreeSet[_]], 144)
      kryo.register(classOf[util.Hashtable[_, _]], 145)
      kryo.register(classOf[ConcurrentHashMap[_, _]], 146)
      kryo.register(classOf[Vector[_]], 148)
      kryo.register(classOf[SimpleDateFormat], 147)
      kryo.register(classOf[Object], 149)
      kryo.register(classOf[Tuple1[_]], 152)
      kryo.register(classOf[Tuple2[_, _]], 153)
      kryo.register(classOf[Tuple3[_, _, _]], 154)
      kryo.register(classOf[Tuple4[_, _, _, _]], 155)
      kryo.register(classOf[Tuple5[_, _, _, _, _]], 156)
      kryo.register(classOf[Tuple6[_, _, _, _, _, _]], 157)
      kryo.register(classOf[Tuple7[_, _, _, _, _, _, _]], 158)
      kryo.register(classOf[Tuple8[_, _, _, _, _, _, _, _]], 159)
      kryo.register(classOf[Tuple9[_, _, _, _, _, _, _, _, _]], 160)
      kryo.register(classOf[Tuple10[_, _, _, _, _, _, _, _, _, _]], 161)
      kryo.register(classOf[Tuple11[_, _, _, _, _, _, _, _, _, _, _]], 162)
      kryo.register(classOf[Tuple12[_, _, _, _, _, _, _, _, _, _, _, _]], 163)
      kryo.register(classOf[Tuple13[_, _, _, _, _, _, _, _, _, _, _, _, _]], 164)
      kryo.register(classOf[Tuple14[_, _, _, _, _, _, _, _, _, _, _, _, _, _]], 165)
      kryo.register(classOf[Tuple15[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _]], 166)
      kryo.register(classOf[Tuple16[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]], 167)
      kryo.register(classOf[Tuple17[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]], 168)
      kryo.register(classOf[Tuple18[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]], 169)
      kryo.register(classOf[Tuple19[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]], 170)
      kryo.register(classOf[Tuple20[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]], 171)
      kryo.register(classOf[Tuple21[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]], 172)
      kryo.register(classOf[Tuple22[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]], 173)

      var regId: Int = 200
      if (kryoMoudleClassMap != null && !kryoMoudleClassMap.isEmpty()) {
        kryoMoudleClassMap.foreach(x => {
          x._2.foreach(i => {
            kryo.register(i, regId)
            regId = regId + 1
          })
        })
      }

      /**
        * 自定义注册，id 必须是10000以后的
        */
      kryoCustomClassRegMap.foreach(x => {
        kryo.register(x._2, x._1)
      })

      kryo
    }
  }

  /**
    * 获得当前线程的 Kryo 实例
    *
    * @return 当前线程的 Kryo 实例
    */
  def getInstance(): Kryo = kryoLocal.get()


  //-----------------------------------------------
  //          序列化/反序列化对象，及类型信息
  //          序列化的结果里，包含类型的信息
  //          反序列化时不再需要提供类型
  //-----------------------------------------------

  /**
    * 将对象【及类型】序列化为字节数组
    *
    * @param obj 任意对象
    * @tparam T 对象的类型
    * @return 序列化后的字节数组
    */
  def writeClassAndObjectToByteArray[T](obj: T): Array[Byte] = {
    var byteArrayOutputStream: ByteArrayOutputStream = null
    var output: Output = null
    try {
      byteArrayOutputStream = new ByteArrayOutputStream()
      output = new Output(byteArrayOutputStream)
      val kryo = getInstance()
      kryo.writeClassAndObject(output, obj)
      output.flush()
      byteArrayOutputStream.toByteArray()
    } finally {
      if (byteArrayOutputStream != null) {
        byteArrayOutputStream.close()
      }

      if (output != null) {
        output.close()
      }
    }

  }

  /**
    * 将对象【及类型】序列化为 String
    * 利用了 Base64 编码
    *
    * @param obj 任意对象
    * @tparam T 对象的类型
    * @return 序列化后的字符串
    */
  def writeToString[T](obj: T): String = try
    new String(Base64.encodeBase64(writeClassAndObjectToByteArray(obj)), DEFAULT_ENCODING)
  catch {
    case e: UnsupportedEncodingException =>
      throw new IllegalStateException(e)
  }

  /**
    * 将字节数组反序列化为原对象
    *
    * @param byteArray writeToByteArray 方法序列化后的字节数组
    * @tparam T 原对象的类型
    * @return 原对象
    */
  @SuppressWarnings(Array("unchecked"))
  def readFromByteArray[T](byteArray: Array[Byte]): T = {
    var byteArrayInputStream: ByteArrayInputStream = null
    var input: Input = null
    var result: Object = null
    try {
      if (byteArray != null) {
        byteArrayInputStream = new ByteArrayInputStream(byteArray)
        input = new Input(byteArrayInputStream)
        val kryo = getInstance()
        result = kryo.readClassAndObject(input)
      }
      result.asInstanceOf[T]
    } finally {
      if (byteArrayInputStream != null) {
        byteArrayInputStream.close()
      }
      if (input != null) {
        input.close()
      }
    }
  }

  /**
    * 将 String 反序列化为原对象
    * 利用了 Base64 编码
    *
    * @param str writeToString 方法序列化后的字符串
    * @tparam T 原对象的类型
    * @return 原对象
    */
  def readFromString[T](str: String): T = {
    try
      readFromByteArray(Base64.decodeBase64(str.getBytes(DEFAULT_ENCODING)))
    catch {
      case e: UnsupportedEncodingException =>
        throw new IllegalStateException(e)
    }
  }


  //          只序列化/反序列化对象
  //          序列化的结果里，不包含类型的信息
  /**
    * 将对象序列化为字节数组
    *
    * @param obj 任意对象
    * @tparam T 对象的类型
    * @return 序列化后的字节数组
    */
  def writeObjectToByteArray[T](obj: T): Array[Byte] = {
    var byteArrayOutputStream: ByteArrayOutputStream = null
    var output: Output = null
    try {
      byteArrayOutputStream = new ByteArrayOutputStream
      output = new Output(byteArrayOutputStream)
      val kryo = getInstance()
      kryo.writeObject(output, obj)
      output.flush()
      byteArrayOutputStream.toByteArray
    } finally {
      if (byteArrayOutputStream != null) {
        byteArrayOutputStream.close()
      }

      if (output != null) {
        output.close()
      }
    }

  }

  /**
    * 将对象序列化为 String
    * 利用了 Base64 编码
    *
    * @param obj 任意对象
    * @tparam T 对象的类型
    * @return 序列化后的字符串
    */
  def writeObjectToString[T](obj: T): String = {
    try
      new String(Base64.encodeBase64(writeObjectToByteArray(obj)), DEFAULT_ENCODING)
    catch {
      case e: UnsupportedEncodingException =>
        throw new IllegalStateException(e)
    }
  }

  /**
    * 将字节数组反序列化为原对象
    *
    * @param byteArray writeToByteArray 方法序列化后的字节数组
    * @param clazz     原对象的 Class
    * @tparam T 原对象的类型
    * @return 原对象
    */
  @SuppressWarnings(Array("unchecked"))
  def readObjectFromByteArray[T](byteArray: Array[Byte], clazz: Class[T]): T = {
    var byteArrayInputStream: ByteArrayInputStream = null
    var input: Input = null
    try {
      byteArrayInputStream = new ByteArrayInputStream(byteArray)
      input = new Input(byteArrayInputStream)
      val kryo = getInstance()
      kryo.readObject(input, clazz)
    } finally {
      if (byteArrayInputStream != null) {
        byteArrayInputStream.close()
      }
      if (input != null) {
        input.close()
      }
    }
  }

  /**
    * 将 String 反序列化为原对象
    * 利用了 Base64 编码
    *
    * @param str   writeToString 方法序列化后的字符串
    * @param clazz 原对象的 Class
    * @tparam  T 原对象的类型
    * @return 原对象
    */
  def readObjectFromString[T](str: String, clazz: Class[T]): T = {
    try
      readObjectFromByteArray(Base64.decodeBase64(str.getBytes(DEFAULT_ENCODING)), clazz)
    catch {
      case e: UnsupportedEncodingException =>
        throw new IllegalStateException(e)
    }
  }

  def register[T](clazz: Class[T]) = {
    val kryo = getInstance()
    kryo.register(clazz)
  }
}
