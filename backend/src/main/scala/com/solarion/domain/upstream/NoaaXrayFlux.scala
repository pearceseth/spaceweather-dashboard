package com.solarion.domain.upstream

import com.solarion.annotation.doc
import zio.json.*

/**
 * Parser for NOAA GOES X-ray flux data.
 *
 * NOAA X-ray flux response is array-of-objects:
 * [{"time_tag": "2026-03-07T14:55:00Z", "satellite": 18, "flux": 7.13e-07, "energy": "0.1-0.8nm", ...}, ...]
 *
 * @see [[https://github.com/sethpearce/spaceweather-dashboard/blob/main/docs/domain/noaa-xray-flux.md Domain Documentation]]
 */
@doc("docs/domain/noaa-xray-flux.md")
object NoaaXrayFlux:
  final case class XrayReading(
    time_tag: String,
    flux: Option[Double],
    energy: Option[String]
  )

  object XrayReading:
    given JsonDecoder[XrayReading] = DeriveJsonDecoder.gen[XrayReading]

  final case class XrayData(fluxClass: Option[String], fluxValue: Option[Double])

  // Derive flare class from flux value (W/m² in 0.1-0.8nm band)
  private def fluxToClass(flux: Double): String =
    if flux >= 1e-4 then
      val level = flux / 1e-4
      f"X${level}%.1f"
    else if flux >= 1e-5 then
      val level = flux / 1e-5
      f"M${level}%.1f"
    else if flux >= 1e-6 then
      val level = flux / 1e-6
      f"C${level}%.1f"
    else if flux >= 1e-7 then
      val level = flux / 1e-7
      f"B${level}%.1f"
    else
      val level = flux / 1e-8
      f"A${level}%.1f"

  def parseLatest(json: String): Option[XrayData] =
    json.fromJson[List[XrayReading]].toOption.flatMap { readings =>
      // Find latest reading for 0.1-0.8nm band (long-wave X-rays used for flare classification)
      readings.findLast(_.energy.exists(_.contains("0.1-0.8"))).map { reading =>
        val fluxClass = reading.flux.map(fluxToClass)
        XrayData(fluxClass, reading.flux)
      }
    }
