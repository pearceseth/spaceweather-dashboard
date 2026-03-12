package com.solarion.domain

import com.solarion.annotation.doc
import zio.json.*
import java.time.Instant

/**
 * Aggregated space weather status response model.
 *
 * All fields except updatedAt are optional to allow partial failure.
 *
 * @see [[https://github.com/sethpearce/spaceweather-dashboard/blob/main/docs/domain/space-weather-status.md Domain Documentation]]
 */
@doc("docs/domain/space-weather-status.md")
final case class SpaceWeatherStatus(
  kp: Option[Double],
  kpSource: Option[String],
  gScale: Option[String],
  gScaleLabel: Option[String],
  bz: Option[Double],
  bt: Option[Double],
  solarWindSpeed: Option[Int],
  solarWindDensity: Option[Double],
  xrayFluxClass: Option[String],
  xrayFluxValue: Option[Double],
  protonFlux: Option[Double],
  protonEventInProgress: Option[Boolean],
  updatedAt: Instant
)

object SpaceWeatherStatus:
  given JsonEncoder[Instant] = JsonEncoder[String].contramap(_.toString)
  given JsonDecoder[Instant] = JsonDecoder[String].map(Instant.parse)
  given JsonEncoder[SpaceWeatherStatus] = DeriveJsonEncoder.gen[SpaceWeatherStatus]
  given JsonDecoder[SpaceWeatherStatus] = DeriveJsonDecoder.gen[SpaceWeatherStatus]

  def gScaleFromKp(kp: Double): (String, String) =
    if kp < 5 then ("G0", "Quiet")
    else if kp < 6 then ("G1", "Minor Storm")
    else if kp < 7 then ("G2", "Moderate Storm")
    else if kp < 8 then ("G3", "Strong Storm")
    else if kp < 9 then ("G4", "Severe Storm")
    else ("G5", "Extreme Storm")
