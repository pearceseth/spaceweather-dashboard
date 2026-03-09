package com.solarion.domain

import zio.json.*

final case class HealthStatus(
  status: String,
  timestamp: Long
)

object HealthStatus:
  given JsonEncoder[HealthStatus] = DeriveJsonEncoder.gen[HealthStatus]
  given JsonDecoder[HealthStatus] = DeriveJsonDecoder.gen[HealthStatus]
