package com.solarion.app

import com.solarion.routes.{HealthRoutes, StatusRoutes}
import com.solarion.services.{HealthServiceLive, SpaceWeatherClientLive, SpaceWeatherStatusServiceLive}
import zio.*
import zio.http.*
import zio.http.netty.NettyConfig
import zio.logging.backend.SLF4J

object Main extends ZIOAppDefault:

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private val routes = HealthRoutes.routes ++ StatusRoutes.routes

  // Use system DNS resolver instead of Netty's async resolver
  private val clientLayer: ZLayer[Any, Throwable, Client] =
    (ZLayer.succeed(ZClient.Config.default) ++
     ZLayer.succeed(NettyConfig.default) ++
     DnsResolver.system) >>> Client.live

  override def run: ZIO[Any, Any, Any] =
    for
      _ <- ZIO.logInfo("Starting spaceweather-backend API on port 8080")
      _ <- Server
            .serve(routes.toHttpApp)
            .provide(
              Server.defaultWithPort(8080),
              clientLayer,
              Scope.default,
              HealthServiceLive.layer,
              SpaceWeatherClientLive.layer,
              SpaceWeatherStatusServiceLive.layer
            )
    yield ()
