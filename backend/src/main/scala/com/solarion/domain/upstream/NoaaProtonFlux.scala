package com.solarion.domain.upstream

import com.solarion.annotation.doc
import zio.json.*

/**
 * Parser for NOAA GOES proton flux data.
 *
 * NOAA Proton flux response is array-of-objects:
 * [{"time_tag": "2025-03-06T12:00:00Z", "satellite": 16, "flux": 1.2, "energy": ">=10 MeV", ...}, ...]
 *
 * @see [[https://github.com/sethpearce/spaceweather-dashboard/blob/main/docs/domain/noaa-proton-flux.md Domain Documentation]]
 */
@doc("docs/domain/noaa-proton-flux.md")
object NoaaProtonFlux:
  final case class ProtonReading(
    time_tag: String,
    flux: Option[Double],
    energy: Option[String]
  )

  object ProtonReading:
    given JsonDecoder[ProtonReading] = DeriveJsonDecoder.gen[ProtonReading]

  final case class ProtonData(flux: Option[Double], eventInProgress: Option[Boolean])

  // Proton event threshold is 10 PFU (particle flux units) for >=10 MeV protons
  private val ProtonEventThreshold = 10.0

  def parseLatest(json: String): Option[ProtonData] =
    json.fromJson[List[ProtonReading]].toOption.flatMap { readings =>
      // Find the latest reading for >=10 MeV protons
      readings.findLast(_.energy.exists(_.contains("10"))).map { reading =>
        val eventInProgress = reading.flux.map(_ >= ProtonEventThreshold)
        ProtonData(reading.flux, eventInProgress)
      }
    }
