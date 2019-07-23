package com.dautechnologies.assetmanagementservice.impl

import java.util.UUID

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import com.dautechnologies.assetmanagementservice.api
import com.dautechnologies.assetmanagementservice.api.{Asset, AssetEntityStateResponse, AssetmanagementserviceService}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker._
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import org.slf4j.{Logger, LoggerFactory}
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Implementation of the AssetmanagementserviceService.
  */

object AssetmanagementserviceServiceImpl extends AssetConfig {

  def createAssetId(name: String) = {
    s"$name-${UUID.randomUUID().toString}"
  }

}

class AssetmanagementserviceServiceImpl(system: ActorSystem,
                                        materialiser: Materializer,
                                        cassandraSession: CassandraSession,
                                        persistentEntityRegistry: PersistentEntityRegistry,
                                        lifecycle: ApplicationLifecycle,
                                        streamingAPITopic: Topic[Asset]
                                       ) extends AssetmanagementserviceService {

  import AssetmanagementserviceServiceImpl._

  streamingAPITopic.subscribe.withMetadata.atLeastOnce(
    Flow[Message[Asset]].map { assetMsg =>
      val ref = persistentEntityRegistry.refFor[AssetmanagementserviceEntity](assetMsg.payload.name)
      val assetImpl = new AssetImpl(createAssetId(assetMsg.payload.name),
        assetMsg.payload.name,
        assetMsg.payload.description,
        assetMsg.payload.traceables)
      ref.ask(UseAsset(assetImpl))
      log.debug("kafka consume asset " + assetMsg.payload)
      Done
    }
  )


  override def hello(id: String) = ServiceCall { _ =>
    // Look up the AssetManagementService entity for the given ID.
    val ref = persistentEntityRegistry.refFor[AssetmanagementserviceEntity](id)

    // Ask the entity the Hello command.
    ref.ask(Hello(id))
  }

  override def useGreeting(id: String) = ServiceCall { request =>
    // Look up the AssetManagementService entity for the given ID.
    val ref = persistentEntityRegistry.refFor[AssetmanagementserviceEntity](id)

    // Tell the entity to use the greeting message specified.
    ref.ask(UseGreetingMessage(request.message))
  }

  override def createAsset(): ServiceCall[Asset, Done] = ServiceCall { request =>
    val id = createAssetId(request.name)
    val pM = new UseAsset(new AssetImpl(id, request.name, request.description, request.traceables))
    persistentEntityRegistry.refFor[AssetmanagementserviceEntity](id).ask(pM)
    log.debug(s"Asset created : $id")
    Future(Done)
  }

  override def assetTopic(): Topic[api.AssetChanged] =
    TopicProducer.singleStreamWithOffset {
      fromOffset =>
        persistentEntityRegistry.eventStream(AssetmanagementserviceEvent.Tag, fromOffset)
          .map(ev => (convertEvent(ev), ev.offset))
    }

  private def convertEvent(assetEvent: EventStreamElement[AssetmanagementserviceEvent]): api.AssetChanged = {
    assetEvent.event match {
      case ac: AssetChanged => api.AssetChanged(ac.assetImpl.id, ac.assetImpl.name, ac.assetImpl.description, ac.assetImpl.traceables)
    }
  }

  override def queryAssetEntityState(assetId: String): ServiceCall[NotUsed, AssetEntityStateResponse] = ServiceCall { _ =>
    val ref = persistentEntityRegistry.refFor[AssetmanagementserviceEntity](assetId)
    ref.ask(new GetCurrentAssetCommand(assetId)).map{ cmr:CurrentAssetReply => new AssetEntityStateResponse( new Asset(cmr.assetImpl.name,cmr.assetImpl.description,cmr.assetImpl.traceables))}
  }
}
