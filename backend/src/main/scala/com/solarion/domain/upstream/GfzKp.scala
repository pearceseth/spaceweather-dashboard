package com.solarion.domain.upstream

import zio.json.*

// GFZ Kp response is object with parallel arrays:
// {"datetime": ["2025-03-06T12:00:00Z", ...], "Kp": [3.7, ...], "ap": [...], ...}
object GfzKp:
  final case class GfzKpResponse(
    datetime: List[String],
    Kp: List[Double]
  )

  object GfzKpResponse:
    given JsonDecoder[GfzKpResponse] = DeriveJsonDecoder.gen[GfzKpResponse]

  def parseLatest(json: String): Option[(Double, String)] =
    json.fromJson[GfzKpResponse]
      .toOption
      .flatMap { response =>
        response.Kp.lastOption.map(_ -> "gfz-estimated")
      }
