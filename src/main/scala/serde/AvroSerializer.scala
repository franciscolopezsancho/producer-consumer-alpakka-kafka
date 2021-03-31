package tester

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import org.apache.avro.Schema
import org.apache.avro.specific.SpecificRecordBase

import com.twitter.bijection.avro.SpecificAvroCodecs
import com.twitter.bijection.Injection

import scala.util.Try



/**
  * This could be used to add to serialize/deserialize in a 'map'
  * inside the streams as oppossed to ser/der from the akka.kafka.{ProducerSettings,ConsumerSettings} 
  * with GenericSerde[T] (where T is the business object class, e.g. SensorData) 
  *     
  * Instead GenericSerde, ProducerSetting/ConsumerSettings should use ByteStringSerializer/ByteStringDeserializer 
  * 
  */
class AvroSerializer[T <: SpecificRecordBase](avroSchema: Schema) {

	val recordInjection: Injection[T, Array[Byte]] = SpecificAvroCodecs.toBinary(avroSchema)

	def decode(bytes: Array[Byte]): Try[T] = recordInjection.invert(bytes)

	def encode(t: T): Array[Byte] = recordInjection(t)

}


