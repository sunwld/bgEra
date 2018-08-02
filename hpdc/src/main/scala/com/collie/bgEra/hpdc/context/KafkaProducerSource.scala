package com.collie.bgEra.hpdc.context

import java.util.Properties

import com.collie.bgEra.commons.util.CommonUtils
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig}
import org.springframework.stereotype.Component
import scala.collection.JavaConversions._
import scala.collection.mutable

@Component
class KafkaProducerSource {
  val kfkProducerSourceMap: mutable.Map[Class[_], Object] = mutable.HashMap()

  def getKfkProducerSource[T](clazz: Class[T]): KafkaProducer[String, T] = {
    val value = kfkProducerSourceMap.getOrElse(clazz, null)
    if (value == null) {
      kfkProducerSourceMap.synchronized {
        if (kfkProducerSourceMap.getOrElse(clazz,null) == null) {
          val producerProp = new Properties()
          producerProp.setProperty(ProducerConfig.ACKS_CONFIG, "1")
          producerProp.setProperty(ProducerConfig.RETRIES_CONFIG, "1")
          producerProp.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, "16384")
          producerProp.setProperty(ProducerConfig.LINGER_MS_CONFIG, "1000")
          producerProp.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy")
          producerProp.setProperty(ProducerConfig.BUFFER_MEMORY_CONFIG, "100663296")
          producerProp.setProperty(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, "1073741824")
          producerProp.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "com.collie.bgEra.hpdc.kafka.serializers.ObjectSerializer")
          producerProp.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "com.collie.bgEra.hpdc.kafka.serializers.ObjectSerializer")
          val customProp = CommonUtils.readPropertiesFile("hpdcKafkaProducer.properties")
          customProp.foreach(p => producerProp.setProperty(p._1, p._2))
          val producer = new KafkaProducer[String, T](producerProp)
          kfkProducerSourceMap.put(clazz, producer.asInstanceOf[Object])
          producer
        } else {
          kfkProducerSourceMap(clazz).asInstanceOf[KafkaProducer[String, T]]
        }
      }
    } else {
      value.asInstanceOf[KafkaProducer[String, T]]
    }
  }

}
