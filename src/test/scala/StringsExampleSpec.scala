import collection.mutable.Stack
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
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer

import akka.stream.scaladsl.{ Sink, Keep, Source}
import akka.stream.ActorMaterializer
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ ConsumerSettings, ProducerSettings}

class StringsExampleSpec extends AsyncFlatSpec with should.Matchers {

  "A Consumer" should "be able to consume" in {

    implicit val actorSystem = ActorSystem(Behaviors.empty, "some")
    implicit val ec          = actorSystem.executionContext

    val bootstrapServers = "localhost:9092"
    val topic = "mytopic"

    val config = actorSystem.settings.config.getConfig("akka.kafka.producer")
    val producerSettings =
      ProducerSettings(config, new StringSerializer, new StringSerializer)
        .withBootstrapServers(bootstrapServers)

    val configConsumer = actorSystem.settings.config.getConfig("akka.kafka.consumer")
    val consumerSettings =
      ConsumerSettings(configConsumer, new StringDeserializer, new StringDeserializer)
        .withBootstrapServers(bootstrapServers)
        .withGroupId("group1")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    val done: Future[Done] =
      Source(1 to 100)
        .map(_.toString)
        .map(value => new ProducerRecord[String, String](topic, s"value $value"))
        .runWith(Producer.plainSink(producerSettings))

    
    val (control, result) = Consumer
      .plainSource(
        consumerSettings,
        Subscriptions.topics(topic)
      )
      .toMat(Sink.seq)(Keep.both)
      .run()

    Thread.sleep(3000)

    done.map(  _ should be(Done))
    control.shutdown().map( _ should be(Done))
    val resultado = Await.result(result, 3.seconds)
    assert(!resultado.filter( _.value() == "value 80" ).isEmpty)
      

  }

}
