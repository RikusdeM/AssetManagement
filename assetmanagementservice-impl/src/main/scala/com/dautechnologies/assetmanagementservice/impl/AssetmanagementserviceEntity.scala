package com.dautechnologies.assetmanagementservice.impl

import java.time.LocalDateTime

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, PersistentEntity}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.typesafe.config.ConfigFactory
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{Format, Json}
import AssetmanagementserviceServiceImpl.createAssetId

import scala.collection.immutable.Seq

class AssetmanagementserviceEntity extends PersistentEntity with AssetConfig {

  override type Command = AssetmanagementserviceCommand[_]
  override type Event = AssetmanagementserviceEvent
  override type State = AssetmanagementserviceState

  /**
    * The initial state. This is used if there is no snapshotted state to be found.
    */
  //  override def initialState: AssetmanagementserviceState = AssetmanagementserviceState("Hello", LocalDateTime.now.toString)

  override def initialState: AssetmanagementserviceState = AssetmanagementserviceState(new AssetImpl(
    createAssetId("testAsset"),
    "testAsset",
    "this is a test Asset",
    Map("sensor_1" -> "0")),
    LocalDateTime.now.toString)

  /**
    * An entity can define different behaviours for different states, so the behaviour
    * is a function of the current state to a set of actions.
    */
  override def behavior: Behavior = {
    case AssetmanagementserviceState(message, _) => Actions()

      .onCommand[UseAsset, Done] {

      // Command handler for the UseAsset command
      case (UseAsset(assetImpl), ctx, state) =>
        log.info(s"Received Asset command $assetImpl")
        // In response to this command, we want to first persist it as a
        // AssetChanged event
        ctx.thenPersist(
          AssetChanged(assetImpl)
        ) { _ =>
          // Then once the event is successfully persisted, we respond with done.
          ctx.reply(Done)
        }
    }

      .onReadOnlyCommand[GetCurrentAssetCommand, CurrentAssetReply] {
      // Command handler for the Hello command
      case (_:GetCurrentAssetCommand, ctx, state) =>
        // Reply with a asset from the current state
        ctx.reply(CurrentAssetReply(state.assetImpl))
    }

      .onEvent {

      // Event handler for the GreetingMessageChanged event
      case (AssetChanged(assetImpl), state) =>
        // We simply update the current state to use the asset from
        // the event.
        AssetmanagementserviceState(assetImpl, LocalDateTime.now().toString)

    }
  }
}

/**
  * The current state held by the persistent entity.
  */
//case class AssetmanagementserviceState(message: String, timestamp: String)
case class AssetmanagementserviceState(assetImpl: AssetImpl, timestamp: String)

object AssetmanagementserviceState {
  /**
    * Format for the hello state.
    *
    * Persisted entities get snapshotted every configured number of events. This
    * means the state gets stored to the database, so that when the entity gets
    * loaded, you don't need to replay all the events, just the ones since the
    * snapshot. Hence, a JSON format needs to be declared so that it can be
    * serialized and deserialized when storing to and from the database.
    */
  implicit val format: Format[AssetmanagementserviceState] = Json.format
}

/**
  * This interface defines all the events that the AssetmanagementserviceEntity supports.
  */
sealed trait AssetmanagementserviceEvent extends AggregateEvent[AssetmanagementserviceEvent] {
  def aggregateTag: AggregateEventTag[AssetmanagementserviceEvent] = AssetmanagementserviceEvent.Tag
}

object AssetmanagementserviceEvent {
  val Tag: AggregateEventTag[AssetmanagementserviceEvent] = AggregateEventTag[AssetmanagementserviceEvent]
}

case class AssetChanged(assetImpl: AssetImpl) extends AssetmanagementserviceEvent

object AssetChanged {
  implicit val format: Format[AssetChanged] = Json.format[AssetChanged]
}

case class AssetImpl(id: String, name: String, description: String, traceables: Map[String, String])

object AssetImpl {
  implicit val format: Format[AssetImpl] = Json.format[AssetImpl]
}

/**
  * This interface defines all the commands that the AssetmanagementserviceEntity supports.
  */
sealed trait AssetmanagementserviceCommand[R] extends ReplyType[R]


case class UseAsset(assetImpl: AssetImpl) extends AssetmanagementserviceCommand[Done]

object UseAsset {
  implicit val format: Format[UseAsset] = Json.format
}

case class GetCurrentAssetCommand(assetId: String) extends AssetmanagementserviceCommand[CurrentAssetReply]

object GetCurrentAssetCommand {
  implicit val format: Format[GetCurrentAssetCommand] = Json.format
}

case class CurrentAssetReply(assetImpl: AssetImpl)

object CurrentAssetReply {
  implicit val format: Format[CurrentAssetReply] = Json.format
}

/**
  * Akka serialization, used by both persistence and remoting, needs to have
  * serializers registered for every type serialized or deserialized. While it's
  * possible to use any serializer you want for Akka messages, out of the box
  * Lagom provides support for JSON, via this registry abstraction.
  *
  * The serializers are registered here, and then provided to Lagom in the
  * application loader.
  */
object AssetmanagementserviceSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[AssetmanagementserviceState],
    JsonSerializer[AssetChanged],
    JsonSerializer[UseAsset],
    JsonSerializer[GetCurrentAssetCommand],
    JsonSerializer[CurrentAssetReply],
    JsonSerializer[AssetImpl]
  )
}

trait AssetConfig {
  lazy val log: Logger = LoggerFactory.getLogger(classOf[AssetmanagementserviceServiceImpl])
  lazy val config = ConfigFactory.load()
}
