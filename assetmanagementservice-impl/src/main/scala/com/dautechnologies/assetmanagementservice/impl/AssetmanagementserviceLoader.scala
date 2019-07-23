package com.dautechnologies.assetmanagementservice.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.dautechnologies.assetmanagementservice.api.{Asset, AssetmanagementserviceService, AssetmanagementserviceStreamService}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry

import com.softwaremill.macwire._
import play.api.Environment

import scala.concurrent.ExecutionContext

class AssetmanagementserviceLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new AssetmanagementserviceApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new AssetmanagementserviceApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[AssetmanagementserviceService])
}

trait AssetmanagementserviceComponents extends LagomServerComponents with CassandraPersistenceComponents
  with LagomKafkaComponents
  with AhcWSComponents {

  implicit def executionContext: ExecutionContext

  def environment: Environment

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry: JsonSerializerRegistry = AssetmanagementserviceSerializerRegistry

  // Register the AssetManagementService persistent entity
  persistentEntityRegistry.register(wire[AssetmanagementserviceEntity])

  //todo: Readside
}

abstract class AssetmanagementserviceApplication(context: LagomApplicationContext)
  extends LagomApplication(context) with AssetmanagementserviceComponents {

  lazy val streamService:AssetmanagementserviceStreamService = serviceClient.implement[AssetmanagementserviceStreamService]
  lazy val topic:Topic[Asset] = streamService.assetmanagementserviceStreamTopic()

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[AssetmanagementserviceService](wire[AssetmanagementserviceServiceImpl])
}
