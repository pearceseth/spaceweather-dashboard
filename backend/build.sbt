val scala3Version = "3.3.3"

val zioVersion        = "2.1.1"
val zioHttpVersion    = "3.0.0-RC6"
val zioLoggingVersion = "2.2.2"
val zioJsonVersion    = "0.6.2"
val zioCacheVersion   = "0.2.3"

lazy val root = project
  .in(file("."))
  .settings(
    name := "spaceweather-backend",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"              % zioVersion,
      "dev.zio" %% "zio-json"         % zioJsonVersion,
      "dev.zio" %% "zio-http"         % zioHttpVersion,
      "dev.zio" %% "zio-cache"        % zioCacheVersion,
      "dev.zio" %% "zio-logging"      % zioLoggingVersion,
      "dev.zio" %% "zio-logging-slf4j2" % zioLoggingVersion,
      "ch.qos.logback" % "logback-classic" % "1.5.6",
      "net.logstash.logback" % "logstash-logback-encoder" % "7.4",

      // Testing
      "dev.zio" %% "zio-test"         % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt"     % zioVersion % Test,
      "dev.zio" %% "zio-test-magnolia" % zioVersion % Test
    ),

    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),

    // Force IPv4 to avoid routing issues with IPv6 addresses from CDN DNS
    fork := true,
    javaOptions += "-Djava.net.preferIPv4Stack=true"
  )
