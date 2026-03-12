package com.solarion.routes

import com.solarion.annotation.doc
import com.solarion.services.HealthService
import zio.*
import zio.http.*
import zio.json.*

/**
 * Health check route handler.
 *
 * Returns basic service health status.
 *
 * @see [[https://github.com/sethpearce/spaceweather-dashboard/blob/main/docs/api/health.md Route Documentation]]
 */
@doc("docs/api/health.md")
object HealthRoutes:
  val routes: Routes[HealthService, Nothing] =
    Routes(
      Method.GET / "health" -> handler {
        for
          status <- HealthService.check
        yield Response.json(status.toJson)
      }
    )
