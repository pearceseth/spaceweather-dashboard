package com.solarion.routes

import com.solarion.services.HealthService
import zio.*
import zio.http.*
import zio.json.*

object HealthRoutes:
  val routes: Routes[HealthService, Nothing] =
    Routes(
      Method.GET / "health" -> handler {
        for
          status <- HealthService.check
        yield Response.json(status.toJson)
      }
    )
