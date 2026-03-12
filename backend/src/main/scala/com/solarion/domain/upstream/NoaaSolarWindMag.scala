package com.solarion.domain.upstream

import com.solarion.annotation.doc
import zio.json.*

/**
 * Parser for NOAA SWPC solar wind magnetic field data.
 *
 * NOAA Solar Wind Mag response is array-of-arrays:
 * [["time_tag","bx_gsm","by_gsm","bz_gsm","lon_gsm","lat_gsm","bt"], ["2026-03-07 14:56:00.000","5.19","-6.82","-0.08","307.28","-0.56","8.57"], ...]
 *
 * @see [[https://github.com/sethpearce/spaceweather-dashboard/blob/main/docs/domain/noaa-solar-wind-mag.md Domain Documentation]]
 */
@doc("docs/domain/noaa-solar-wind-mag.md")
object NoaaSolarWindMag:
  final case class MagData(bz: Option[Double], bt: Option[Double])

  def parseLatest(json: String): Option[MagData] =
    json.fromJson[List[List[String]]].toOption.flatMap { rows =>
      // Skip header, find last valid row
      rows.drop(1).findLast { row =>
        row.lift(3).exists(v => v != "-9999" && v.toDoubleOption.isDefined) ||
        row.lift(6).exists(v => v != "-9999" && v.toDoubleOption.isDefined)
      }.map { row =>
        val bz = row.lift(3).flatMap(v => if v == "-9999" then None else v.toDoubleOption)
        val bt = row.lift(6).flatMap(v => if v == "-9999" then None else v.toDoubleOption)
        MagData(bz, bt)
      }
    }
