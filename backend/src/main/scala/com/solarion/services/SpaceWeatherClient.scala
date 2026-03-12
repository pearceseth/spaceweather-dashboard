package com.solarion.services

import com.solarion.annotation.doc
import com.solarion.domain.error.{DataSource, FetchError, NetworkError}
import com.solarion.domain.upstream.*
import zio.*
import zio.http.*

/**
 * HTTP client for fetching upstream space weather data.
 *
 * Fetches from NOAA SWPC and GFZ Potsdam APIs.
 *
 * @see [[https://github.com/sethpearce/spaceweather-dashboard/blob/main/docs/services/space-weather-client.md Service Documentation]]
 */
@doc("docs/services/space-weather-client.md")
trait SpaceWeatherClient:
  def fetchNoaaKpIndex: ZIO[Scope, FetchError, Option[(Double, String)]]
  def fetchNoaaSolarWindPlasma: ZIO[Scope, FetchError, Option[NoaaSolarWindPlasma.PlasmaData]]
  def fetchNoaaSolarWindMag: ZIO[Scope, FetchError, Option[NoaaSolarWindMag.MagData]]
  def fetchNoaaXrayFlux: ZIO[Scope, FetchError, Option[NoaaXrayFlux.XrayData]]
  def fetchNoaaProtonFlux: ZIO[Scope, FetchError, Option[NoaaProtonFlux.ProtonData]]
  def fetchGfzKp: ZIO[Scope, FetchError, Option[(Double, String)]]

object SpaceWeatherClient:
  def fetchNoaaKpIndex: ZIO[SpaceWeatherClient & Scope, FetchError, Option[(Double, String)]] =
    ZIO.serviceWithZIO[SpaceWeatherClient](_.fetchNoaaKpIndex)

  def fetchNoaaSolarWindPlasma: ZIO[SpaceWeatherClient & Scope, FetchError, Option[NoaaSolarWindPlasma.PlasmaData]] =
    ZIO.serviceWithZIO[SpaceWeatherClient](_.fetchNoaaSolarWindPlasma)

  def fetchNoaaSolarWindMag: ZIO[SpaceWeatherClient & Scope, FetchError, Option[NoaaSolarWindMag.MagData]] =
    ZIO.serviceWithZIO[SpaceWeatherClient](_.fetchNoaaSolarWindMag)

  def fetchNoaaXrayFlux: ZIO[SpaceWeatherClient & Scope, FetchError, Option[NoaaXrayFlux.XrayData]] =
    ZIO.serviceWithZIO[SpaceWeatherClient](_.fetchNoaaXrayFlux)

  def fetchNoaaProtonFlux: ZIO[SpaceWeatherClient & Scope, FetchError, Option[NoaaProtonFlux.ProtonData]] =
    ZIO.serviceWithZIO[SpaceWeatherClient](_.fetchNoaaProtonFlux)

  def fetchGfzKp: ZIO[SpaceWeatherClient & Scope, FetchError, Option[(Double, String)]] =
    ZIO.serviceWithZIO[SpaceWeatherClient](_.fetchGfzKp)

final case class SpaceWeatherClientLive(client: Client) extends SpaceWeatherClient:

  private val NoaaKpIndexUrl = "https://services.swpc.noaa.gov/products/noaa-planetary-k-index.json"
  private val NoaaSolarWindPlasmaUrl = "https://services.swpc.noaa.gov/products/solar-wind/plasma-1-day.json"
  private val NoaaSolarWindMagUrl = "https://services.swpc.noaa.gov/products/solar-wind/mag-1-day.json"
  private val NoaaXrayFluxUrl = "https://services.swpc.noaa.gov/json/goes/primary/xrays-1-day.json"
  private val NoaaProtonFluxUrl = "https://services.swpc.noaa.gov/json/goes/primary/integral-protons-1-day.json"
  private val GfzKpUrl = "https://kp.gfz.de/app/json/?start=now-1h&end=now"

  private def fetchJson(url: String, dataSource: DataSource): ZIO[Scope, FetchError, String] =
    client.request(Request.get(URL.decode(url).toOption.get))
      .flatMap(_.body.asString)
      .mapError { t =>
        val message = Option(t.getMessage).getOrElse(t.getClass.getSimpleName)
        NetworkError(dataSource, message, Some(url), Some(t))
      }

  override def fetchNoaaKpIndex: ZIO[Scope, FetchError, Option[(Double, String)]] =
    fetchJson(NoaaKpIndexUrl, DataSource.NoaaKpIndex).map(NoaaKpIndex.parseLatest)

  override def fetchNoaaSolarWindPlasma: ZIO[Scope, FetchError, Option[NoaaSolarWindPlasma.PlasmaData]] =
    fetchJson(NoaaSolarWindPlasmaUrl, DataSource.NoaaSolarWindPlasma).map(NoaaSolarWindPlasma.parseLatest)

  override def fetchNoaaSolarWindMag: ZIO[Scope, FetchError, Option[NoaaSolarWindMag.MagData]] =
    fetchJson(NoaaSolarWindMagUrl, DataSource.NoaaSolarWindMag).map(NoaaSolarWindMag.parseLatest)

  override def fetchNoaaXrayFlux: ZIO[Scope, FetchError, Option[NoaaXrayFlux.XrayData]] =
    fetchJson(NoaaXrayFluxUrl, DataSource.NoaaXrayFlux).map(NoaaXrayFlux.parseLatest)

  override def fetchNoaaProtonFlux: ZIO[Scope, FetchError, Option[NoaaProtonFlux.ProtonData]] =
    fetchJson(NoaaProtonFluxUrl, DataSource.NoaaProtonFlux).map(NoaaProtonFlux.parseLatest)

  override def fetchGfzKp: ZIO[Scope, FetchError, Option[(Double, String)]] =
    fetchJson(GfzKpUrl, DataSource.GfzKp).map(GfzKp.parseLatest)

object SpaceWeatherClientLive:
  val layer: ZLayer[Client, Nothing, SpaceWeatherClient] =
    ZLayer.fromFunction(SpaceWeatherClientLive(_))
