package com.solarion.domain.error

sealed trait DomainError extends Throwable {
  val message: String
  val context: Option[String]
}

case class ModelCreationError(message: String, context: Option[String]) extends DomainError

// Data source enum for structured logging
enum DataSource(val name: String):
  case GfzKp extends DataSource("gfz-kp")
  case NoaaKpIndex extends DataSource("noaa-kp-index")
  case NoaaSolarWindPlasma extends DataSource("noaa-solar-wind-plasma")
  case NoaaSolarWindMag extends DataSource("noaa-solar-wind-mag")
  case NoaaXrayFlux extends DataSource("noaa-xray-flux")
  case NoaaProtonFlux extends DataSource("noaa-proton-flux")

// Sealed trait for fetch errors
sealed trait FetchError extends DomainError:
  val dataSource: DataSource

case class NetworkError(dataSource: DataSource, message: String, context: Option[String], cause: Option[Throwable] = None) extends FetchError
case class ParseError(dataSource: DataSource, message: String, context: Option[String]) extends FetchError
