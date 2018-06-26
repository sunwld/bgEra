package com.collie.bgEra.cloudApp.utils

import org.objenesis.strategy.StdInstantiatorStrategy
import org.springframework.stereotype.Component
import com.esotericsoftware.kryo.io.{Input, Output}
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.UnsupportedEncodingException
import com.esotericsoftware.kryo.Kryo

import org.apache.commons.codec.binary.Base64

@Component
class KryoUtil {
  private val DEFAULT_ENCODING = "UTF-8"
  //每个线程的 Kryo 实例//每个线程的 Kryo 实例

  private val kryoLocal = new ThreadLocal[Kryo]() {
    override protected def initialValue: Kryo = {
      val kryo = new Kryo

      /**
        * 不要轻易改变这里的配置！更改之后，序列化的格式就会发生变化，
        * 上线的同时就必须清除 Redis 里的所有缓存，
        * 否则那些缓存再回来反序列化的时候，就会报错
        */
      //支持对象循环引用（否则会栈溢出）
      kryo.setReferences(true) //默认值就是 true，添加此行的目的是为了提醒维护者，不要改变这个配置

      //不强制要求注册类（注册行为无法保证多个 JVM 内同一个类的注册编号相同；而且业务系统中大量的 Class 也难以一一注册）
      kryo.setRegistrationRequired(false) //默认值就是 false，添加此行的目的是为了提醒维护者，不要改变这个配置

      //Fix the NPE bug when deserializing Collections.
      kryo.getInstantiatorStrategy.asInstanceOf[Kryo.DefaultInstantiatorStrategy].setFallbackInstantiatorStrategy(new StdInstantiatorStrategy)
      kryo
    }
  }

  /**
    * 获得当前线程的 Kryo 实例
    *
    * @return 当前线程的 Kryo 实例
    */
  def getInstance: Kryo = kryoLocal.get


  //-----------------------------------------------
  //          序列化/反序列化对象，及类型信息
  //          序列化的结果里，包含类型的信息
  //          反序列化时不再需要提供类型
  //-----------------------------------------------

  /**
    * 将对象【及类型】序列化为字节数组
    * @param obj 任意对象
    * @tparam T 对象的类型
    * @return  序列化后的字节数组
    */
  def writeClassAndObjectToByteArray[T](obj: T): Array[Byte] = {
    var byteArrayOutputStream: ByteArrayOutputStream = null
    var output: Output = null
    try{
      byteArrayOutputStream = new ByteArrayOutputStream
      output = new Output(byteArrayOutputStream)
      val kryo = getInstance
      kryo.writeClassAndObject(output, obj)
      output.flush()
      byteArrayOutputStream.toByteArray
    } finally {
      if(byteArrayOutputStream != null){
        byteArrayOutputStream.close()
      }

      if(output != null){
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
    try {
      byteArrayInputStream = new ByteArrayInputStream(byteArray)
      input = new Input(byteArrayInputStream)
      val kryo = getInstance
      kryo.readClassAndObject(input).asInstanceOf[T]
    } finally {
      if(byteArrayInputStream != null){
        byteArrayInputStream.close()
      }
      if(input != null){
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
      val kryo = getInstance
      kryo.writeObject(output, obj)
      output.flush()
      byteArrayOutputStream.toByteArray
    } finally{
      if(byteArrayOutputStream != null){
        byteArrayOutputStream.close()
      }

      if(output != null){
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
      val kryo = getInstance
      kryo.readObject(input, clazz)
    } finally{
      if(byteArrayInputStream != null){
        byteArrayInputStream.close()
      }
      if(input != null){
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
}
