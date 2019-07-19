package com.dautechnologies.assetmanagementservice.impl

import com.dautechnologies.assetmanagementservice.api
import com.dautechnologies.assetmanagementservice.api.AssetmanagementserviceService
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}

/**
  * Implementation of the AssetmanagementserviceService.
  */
class AssetmanagementserviceServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends AssetmanagementserviceService {

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


  override def greetingsTopic(): Topic[api.GreetingMessageChanged] =
    TopicProducer.singleStreamWithOffset {
      fromOffset =>
        persistentEntityRegistry.eventStream(AssetmanagementserviceEvent.Tag, fromOffset)
          .map(ev => (convertEvent(ev), ev.offset))
    }

  private def convertEvent(helloEvent: EventStreamElement[AssetmanagementserviceEvent]): api.GreetingMessageChanged = {
    helloEvent.event match {
      case GreetingMessageChanged(msg) => api.GreetingMessageChanged(helloEvent.entityId, msg)
    }
  }
}
