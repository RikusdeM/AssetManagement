package com.dautechnologies.assetmanagementservicestream.api

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}

/**
  * The AssetManagementService stream interface.
  *
  * This describes everything that Lagom needs to know about how to serve and
  * consume the AssetmanagementserviceStream service.
  */
trait AssetmanagementserviceStreamService extends Service {

  def stream: ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]

  override final def descriptor: Descriptor = {
    import Service._

    named("assetmanagementservice-stream")
      .withCalls(
        namedCall("stream", stream)
      ).withAutoAcl(true)
  }
}

