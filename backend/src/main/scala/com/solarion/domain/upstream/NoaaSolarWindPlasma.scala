package com.solarion.domain.upstream

import com.solarion.annotation.doc
import zio.json.*

/**
 * Parser for NOAA SWPC solar wind plasma data.
 *
 * NOAA Solar Wind Plasma response is array-of-arrays:
 * [["time_tag","density","speed","temperature"], ["2026-03-07 14:56:00.000","3.30","464.5","78809"], ...]
 *
 * @see [[https://github.com/sethpearce/spaceweather-dashboard/blob/main/docs/domain/noaa-solar-wind-plasma.md Domain Documentation]]
 */
@doc("docs/domain/noaa-solar-wind-plasma.md")
object NoaaSolarWindPlasma:
  final case class PlasmaData(speed: Option[Int], density: Option[Double])

  def parseLatest(json: String): Option[PlasmaData] =
    json.fromJson[List[List[String]]].toOption.flatMap { rows =>
      // Skip header, find last valid row (not missing data marked as -9999)
      rows.drop(1).findLast { row =>
        row.lift(1).exists(v => v != "-9999" && v.toDoubleOption.isDefined) ||
        row.lift(2).exists(v => v != "-9999" && v.toDoubleOption.isDefined)
      }.map { row =>
        val density = row.lift(1).flatMap(v => if v == "-9999" then None else v.toDoubleOption)
        val speed = row.lift(2).flatMap(v => if v == "-9999" then None else v.toDoubleOption.map(_.toInt))
        PlasmaData(speed, density)
      }
    }
