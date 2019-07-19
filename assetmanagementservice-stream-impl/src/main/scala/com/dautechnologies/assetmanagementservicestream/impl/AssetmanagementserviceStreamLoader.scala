package com.dautechnologies.assetmanagementservicestream.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.dautechnologies.assetmanagementservicestream.api.AssetmanagementserviceStreamService
import com.dautechnologies.assetmanagementservice.api.AssetmanagementserviceService
import com.softwaremill.macwire._

class AssetmanagementserviceStreamLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new AssetmanagementserviceStreamApplication(context) {
      override def serviceLocator: NoServiceLocator.type = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new AssetmanagementserviceStreamApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[AssetmanagementserviceStreamService])
}

abstract class AssetmanagementserviceStreamApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[AssetmanagementserviceStreamService](wire[AssetmanagementserviceStreamServiceImpl])

  // Bind the AssetmanagementserviceService client
  lazy val assetmanagementserviceService: AssetmanagementserviceService = serviceClient.implement[AssetmanagementserviceService]
}
