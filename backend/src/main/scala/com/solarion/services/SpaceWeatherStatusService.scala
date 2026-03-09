package com.solarion.services

import com.solarion.domain.SpaceWeatherStatus
import com.solarion.services.FetchOps.*
import zio.{cache, *}
import zio.cache.*

trait SpaceWeatherStatusService:
  def getStatus: UIO[SpaceWeatherStatus]

object SpaceWeatherStatusService:
  def getStatus: URIO[SpaceWeatherStatusService, SpaceWeatherStatus] =
    ZIO.serviceWithZIO[SpaceWeatherStatusService](_.getStatus)

final case class SpaceWeatherStatusServiceLive(
  client: SpaceWeatherClient,
  cache: Cache[Unit, Nothing, SpaceWeatherStatus]
) extends SpaceWeatherStatusService {
  override def getStatus: UIO[SpaceWeatherStatus] =
    cache.get(())
}

object SpaceWeatherStatusServiceLive:
  private val CacheTtl = 1.minute

  private def fetchAndAggregate(client: SpaceWeatherClient): URIO[Scope, SpaceWeatherStatus] =
    // Use <&> (zipPar) to run in parallel while preserving types
    val fetches =
      client.fetchGfzKp.logFailures <&>
      client.fetchNoaaKpIndex.logFailures <&>
      client.fetchNoaaSolarWindPlasma.logFailures <&>
      client.fetchNoaaSolarWindMag.logFailures <&>
      client.fetchNoaaXrayFlux.logFailures <&>
      client.fetchNoaaProtonFlux.logFailures

    (Clock.instant <&> fetches).map { case (now, (gfzKp, noaaKp, plasma, mag, xray, proton)) =>
      val (kpValue, kpSource) = gfzKp.orElse(noaaKp).unzip // GFZ Kp takes precedence over NOAA Kp
      val (gScale, gScaleLabel) = kpValue.map(SpaceWeatherStatus.gScaleFromKp).unzip

      SpaceWeatherStatus(
        kp = kpValue,
        kpSource = kpSource,
        gScale = gScale,
        gScaleLabel = gScaleLabel,
        bz = mag.flatMap(_.bz),
        bt = mag.flatMap(_.bt),
        solarWindSpeed = plasma.flatMap(_.speed),
        solarWindDensity = plasma.flatMap(_.density),
        xrayFluxClass = xray.flatMap(_.fluxClass),
        xrayFluxValue = xray.flatMap(_.fluxValue),
        protonFlux = proton.flatMap(_.flux),
        protonEventInProgress = proton.flatMap(_.eventInProgress),
        updatedAt = now
      )
    }

  val layer: ZLayer[SpaceWeatherClient & Scope, Nothing, SpaceWeatherStatusService] =
    ZLayer.scoped {
      for
        client <- ZIO.service[SpaceWeatherClient]
        scope  <- ZIO.service[Scope]
        cache <- Cache.make(
          capacity = 1,
          timeToLive = CacheTtl,
          lookup = Lookup[Unit, Any, Nothing, SpaceWeatherStatus](_ =>
            fetchAndAggregate(client).provideEnvironment(ZEnvironment(scope))
          )
        )
      yield SpaceWeatherStatusServiceLive(client, cache)
    }
