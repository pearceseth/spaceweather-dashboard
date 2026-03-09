package com.solarion.domain.upstream

import zio.json.*

// NOAA Kp index response is array-of-arrays:
// [["time_tag","Kp","Kp_fraction",...], ["2025-03-06 12:00:00","3","3.33",...], ...]
// First row is header, subsequent rows are data
object NoaaKpIndex:
  // Response format: [["time_tag","Kp","a_running","station_count"], ["2026-03-01 00:00:00.000","2.67","12","8"], ...]
  def parseLatest(json: String): Option[(Double, String)] =
    json.fromJson[List[List[String]]]
      .toOption
      .flatMap { rows =>
        // Skip header (first row), get last data row
        rows.drop(1).lastOption.flatMap { row =>
          row.lift(1).flatMap(_.toDoubleOption).map(_ -> "noaa-finalized")
        }
    }
