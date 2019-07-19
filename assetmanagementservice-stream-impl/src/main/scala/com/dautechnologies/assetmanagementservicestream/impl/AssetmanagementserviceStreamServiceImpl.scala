package com.dautechnologies.assetmanagementservicestream.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.dautechnologies.assetmanagementservicestream.api.AssetmanagementserviceStreamService
import com.dautechnologies.assetmanagementservice.api.AssetmanagementserviceService

import scala.concurrent.Future

/**
  * Implementation of the AssetmanagementserviceStreamService.
  */
class AssetmanagementserviceStreamServiceImpl(assetmanagementserviceService: AssetmanagementserviceService) extends AssetmanagementserviceStreamService {
  def stream = ServiceCall { hellos =>
    Future.successful(hellos.mapAsync(8)(assetmanagementserviceService.hello(_).invoke()))
  }
}
