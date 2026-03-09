package com.solarion.services

import com.solarion.domain.error.{FetchError, NetworkError}
import zio.*

object FetchOps:
  extension [R, A](effect: ZIO[R, FetchError, Option[A]])
    def logFailures: URIO[R, Option[A]] =
      effect
        .tapError { error =>
          val causeInfo = error match
            case NetworkError(_, _, _, Some(cause: Throwable)) => s" [${cause.getClass.getName}: ${cause.getMessage}]"
            case _ => ""
          ZIO.logAnnotate("dataSource", error.dataSource.name)(
            ZIO.logAnnotate("errorType", error.getClass.getSimpleName)(
              ZIO.logWarning(s"Fetch failed for ${error.dataSource.name}: ${error.message}$causeInfo")
            )
          )
        }
        .option
        .map(_.flatten)
