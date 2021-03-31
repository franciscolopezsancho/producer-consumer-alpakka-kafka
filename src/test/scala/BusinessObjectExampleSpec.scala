package tester

import org.scalatest._
import flatspec._
import matchers._

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



class BusinessObjectExampleSpec extends AnyFlatSpec with should.Matchers {

  "A Consumer" should "be able to consume" in {

    val sensorData = sensordata.SensorData("deviceId",22)

    implicit val actorSystem = ActorSystem(Behaviors.empty, "some")
    implicit val ec          = actorSystem.executionContext

    val bootstrapServers = "localhost:9092"
    val topic = "mytopic"

    val config = actorSystem.settings.config.getConfig("akka.kafka.producer")
    val producerSettings =
      ProducerSettings(config, new StringSerializer, new GenericSerde[SensorData](BinaryFormat))
        .withBootstrapServers(bootstrapServers)


    val configConsumer = actorSystem.settings.config.getConfig("akka.kafka.consumer")
    val consumerSettings =
      ConsumerSettings(configConsumer, new StringDeserializer, new GenericSerde[SensorData](BinaryFormat))
        .withBootstrapServers(bootstrapServers)
        .withGroupId("groupA")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")


    val done: Future[Done] =
      Source(1 to 100)
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

    done.map(  _ should be(Done))
    control.shutdown().map( _ should be(Done))
    val resultado = Await.result(result, 3.seconds)
    println(resultado)
    assert(!resultado.filter( _.value() == SensorData("80",80)).isEmpty)

  }



}
