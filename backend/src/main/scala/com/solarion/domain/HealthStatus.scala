package com.solarion.domain

import com.solarion.annotation.doc
import zio.json.*

/**
 * Health check response model.
 *
 * @see [[https://github.com/sethpearce/spaceweather-dashboard/blob/main/docs/domain/health-status.md Domain Documentation]]
 */
@doc("docs/domain/health-status.md")
final case class HealthStatus(
  status: String,
  timestamp: Long
)

object HealthStatus:
  given JsonEncoder[HealthStatus] = DeriveJsonEncoder.gen[HealthStatus]
  given JsonDecoder[HealthStatus] = DeriveJsonDecoder.gen[HealthStatus]
