package com.collie.bgEra.hpdc.kafka.serializers

import java.util

import com.collie.bgEra.cloudApp.kryoUtil.KryoUtil
import org.apache.kafka.common.serialization.Serializer

class ObjectSerializer extends Serializer[Object]{
  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {

  }

  override def serialize(topic: String, data: Object): Array[Byte] = {
    if(data == null){
      null
    }else{
      KryoUtil.writeClassAndObjectToByteArray(data)
    }
  }

  override def close(): Unit = {

  }
}
