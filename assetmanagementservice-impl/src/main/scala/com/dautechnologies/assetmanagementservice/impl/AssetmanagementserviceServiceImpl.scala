package com.dautechnologies.assetmanagementservice.impl

import java.util.UUID

import akka.Done
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.dautechnologies.assetmanagementservice.api
import com.dautechnologies.assetmanagementservice.api.{Asset, AssetmanagementserviceService}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
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
                                        steamingAPITopic: Topic[Asset]
                                       ) extends AssetmanagementserviceService {

  import AssetmanagementserviceServiceImpl._




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
    val pM = new AssetChanged(id, request.name, request.description, request.traceables)
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
      case ac: AssetChanged => api.AssetChanged(ac.id, ac.name, ac.description, ac.traceables)
    }
  }
}
