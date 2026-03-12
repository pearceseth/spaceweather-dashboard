package com.solarion.routes

import com.solarion.annotation.doc
import com.solarion.services.SpaceWeatherStatusService
import zio.*
import zio.http.*
import zio.json.*

/**
 * Aggregated space weather status route handler.
 *
 * Fans out to NOAA SWPC and GFZ Potsdam endpoints in parallel.
 * Every field in the response is independently nullable — if one upstream
 * source fails, the remaining data is still returned.
 *
 * @see [[https://github.com/sethpearce/spaceweather-dashboard/blob/main/docs/api/status.md Route Documentation]]
 */
@doc("docs/api/status.md")
object StatusRoutes:
  val routes: Routes[SpaceWeatherStatusService, Nothing] =
    Routes(
      Method.GET / "api" / "status" -> handler {
        for
          status <- SpaceWeatherStatusService.getStatus
        yield Response.json(status.toJson)
      }
    )
