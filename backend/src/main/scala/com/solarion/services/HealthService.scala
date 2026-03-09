package com.solarion.services

import com.solarion.domain.HealthStatus
import zio.*

trait HealthService:
  def check: UIO[HealthStatus]

object HealthService:
  def check: URIO[HealthService, HealthStatus] =
    ZIO.serviceWithZIO[HealthService](_.check)

final case class HealthServiceLive() extends HealthService:
  override def check: UIO[HealthStatus] =
    Clock.currentTime(java.util.concurrent.TimeUnit.MILLISECONDS).map { ts =>
      HealthStatus(status = "ok", timestamp = ts)
    }

object HealthServiceLive:
  val layer: ULayer[HealthService] = ZLayer.succeed(HealthServiceLive())
