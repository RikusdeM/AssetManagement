package com.dautechnologies.assetmanagementservice.api

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}


/**
  * The AssetManagementService stream interface.
  *
  * This describes everything that Lagom needs to know about how to serve and
  * consume the AssetmanagementserviceStream service.
  */

object AssetmanagementserviceStreamService{
  val TOPIC = "assetmanagement-input-topic"
}

trait AssetmanagementserviceStreamService extends Service {

  def assetmanagementserviceStreamTopic(): Topic[Asset]

  override final def descriptor: Descriptor = {
    import Service._

    named("assetmanagementservice-stream")
      .withTopics(
        topic(AssetmanagementserviceStreamService.TOPIC,assetmanagementserviceStreamTopic())
      ).withAutoAcl(true)
  }
}

