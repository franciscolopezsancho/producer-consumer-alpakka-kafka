package sample


import akka.actor.typed.{ ActorSystem }
import akka.actor.typed.scaladsl.Behaviors

import akka.Done
import scala.concurrent.{ Future, Await }
import scala.concurrent.duration._
import scala.util.{Success, Failure}

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer

import akka.stream.scaladsl.{ Sink, Keep, Source}
import akka.stream.ActorMaterializer
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ ConsumerSettings, ProducerSettings}
import com.sksamuel.avro4s.BinaryFormat
import com.sksamuel.avro4s.kafka.GenericSerde
import sensordata.SensorData

import org.slf4j.LoggerFactory

object Main {
	
	def main(args: Array[String]): Unit = {

		val log = LoggerFactory.getLogger("Main")

		//TODO improve input management with defaults
		//TODO add docs
		val bootstrapServers = args(0)
	    val topic = args(1)
	    val offset = args(2)
	    val groupId = args(3)	


	    implicit val actorSystem = ActorSystem(Behaviors.empty, "kafkaProdCons")
	    implicit val ec          = actorSystem.executionContext

	    val config = actorSystem.settings.config.getConfig("akka.kafka.producer")
	    val producerSettings =
	      ProducerSettings(config, new StringSerializer, new GenericSerde[SensorData](BinaryFormat))
	        .withBootstrapServers(bootstrapServers)


	    val configConsumer = actorSystem.settings.config.getConfig("akka.kafka.consumer")
	    val consumerSettings =
	      ConsumerSettings(configConsumer, new StringDeserializer, new GenericSerde[SensorData](BinaryFormat))
	        .withBootstrapServers(bootstrapServers)
	        .withGroupId(groupId)
	        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offset)


	    val source = Source(1 to 100)

	    val done: Future[Done] =
	      source
	        .map(value => new ProducerRecord[String, SensorData](topic, SensorData(s"$value",value)))
	        .runWith(Producer.plainSink(producerSettings))

	     val (control, result) = Consumer
	      .plainSource(
	        consumerSettings,
	        Subscriptions.topics(topic)
	      )
	      .toMat(Sink.seq)(Keep.both)
	      .run()

	    Thread.sleep(6000)

	    control.shutdown()
	    actorSystem.terminate()

	    val resultado = Await.result(result, 3.seconds)
	    resultado.foreach( res => log.debug("value {}",res.value()))

	    //testing one value 
	    assert(!resultado.filter( _.value() == SensorData("80",80)).isEmpty) 
	    //testing all the values
	    source.map{ each => 
	    	assert(!resultado.filter( _.value() == SensorData(each.toString,each)).isEmpty)
		}
	    


	}

}