package com.dautechnologies.assetmanagementservice.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.transport.Method._
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.libs.json.{Format, Json}

object AssetmanagementserviceService {
  val TOPIC_NAME = "AssetEvent"
}

/**
  * The AssetManagementService service interface.
  * <p>
  * This describes everything that Lagom needs to know about how to serve and
  * consume the AssetmanagementserviceService.
  */
trait AssetmanagementserviceService extends Service {

  /**
    * Example: curl http://localhost:9000/api/hello/Alice
    */
  def hello(id: String): ServiceCall[NotUsed, String]

  /**
    * Example: curl -H "Content-Type: application/json" -X POST -d '{"message":
    * "Hi"}' http://localhost:9000/api/hello/Alice
    */
  def useGreeting(id: String): ServiceCall[GreetingMessage, Done]

  def createAsset():ServiceCall[Asset,Done]


  /**
    * This gets published to Kafka.
    */
//  def greetingsTopic(): Topic[GreetingMessageChanged]

  def assetTopic(): Topic[AssetChanged]

  override final def descriptor: Descriptor = {
    import Service._
    // @formatter:off
    named("assetmanagementservice")
      .withCalls(
        pathCall("/api/hello/:id", hello _),
        pathCall("/api/hello/:id", useGreeting _),

        restCall(POST,"assets/create",createAsset _)
      )
      .withTopics(
        topic(AssetmanagementserviceService.TOPIC_NAME, assetTopic() _)
          // Kafka partitions messages, messages within the same partition will
          // be delivered in order, to ensure that all messages for the same user
          // go to the same partition (and hence are delivered in order with respect
          // to that user), we configure a partition key strategy that extracts the
          // name as the partition key.
          .addProperty(
          KafkaProperties.partitionKeyStrategy,
          PartitionKeyStrategy[AssetChanged](_.id)
        )
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}

trait command
trait event

case class Asset(name: String, description: String, traceables: Map[String, String]) extends command

object Asset {
  implicit val format: Format[Asset] = Json.format[Asset]
}



/**
  * Message(Event) used by topic stream
  * @param id
  * @param name
  * @param description
  * @param traceables
  */
case class AssetChanged(id:String,name:String,description:String, traceables:Map[String,String]) extends event

object AssetChanged{
  implicit val format:Format[AssetChanged] = Json.format[AssetChanged]
}

/**
  * The greeting message class.
  */
case class GreetingMessage(message: String)

object GreetingMessage {
  /**
    * Format for converting greeting messages to and from JSON.
    *
    * This will be picked up by a Lagom implicit conversion from Play's JSON format to Lagom's message serializer.
    */
  implicit val format: Format[GreetingMessage] = Json.format[GreetingMessage]
}


/**
  * The greeting message class used by the topic stream.
  * Different than [[GreetingMessage]], this message includes the name (id).
  */
case class GreetingMessageChanged(name: String, message: String)

object GreetingMessageChanged {
  /**
    * Format for converting greeting messages to and from JSON.
    *
    * This will be picked up by a Lagom implicit conversion from Play's JSON format to Lagom's message serializer.
    */
  implicit val format: Format[GreetingMessageChanged] = Json.format[GreetingMessageChanged]
}
