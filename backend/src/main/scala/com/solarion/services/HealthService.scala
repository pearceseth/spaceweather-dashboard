package com.solarion.services

import com.solarion.annotation.doc
import com.solarion.domain.HealthStatus
import zio.*

/**
 * Health check service trait.
 *
 * @see [[https://github.com/sethpearce/spaceweather-dashboard/blob/main/docs/services/health-service.md Service Documentation]]
 */
@doc("docs/services/health-service.md")
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
