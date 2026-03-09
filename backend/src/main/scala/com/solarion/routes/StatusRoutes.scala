package com.solarion.routes

import com.solarion.services.SpaceWeatherStatusService
import zio.*
import zio.http.*
import zio.json.*

object StatusRoutes:
  val routes: Routes[SpaceWeatherStatusService, Nothing] =
    Routes(
      Method.GET / "api" / "status" -> handler {
        for
          status <- SpaceWeatherStatusService.getStatus
        yield Response.json(status.toJson)
      }
    )
