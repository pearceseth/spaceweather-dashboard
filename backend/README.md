# Space Weather Backend API

A ZIO-based Scala API server that aggregates real-time space weather data from multiple sources.

## Overview

The backend fetches and aggregates data from:
- **GFZ Potsdam** - Kp geomagnetic index (primary source)
- **NOAA SWPC** - Kp index, solar wind plasma, magnetic field, X-ray flux, proton flux

Data is cached and served via a REST API with automatic fallback when individual sources fail.

## Requirements

- JDK 21+
- sbt 1.10+

## Running

```bash
# Start the server on port 8080
sbt run

# With pretty-printed JSON logs
sbt run | jq .

# With formatted log output
sbt run | jq -r '"\(.["@timestamp"] | split(".")[0]) \(.level) - \(.message)" + if .dataSource then " [dataSource=\(.dataSource)]" else "" end'
```

## API Endpoints

- `GET /api/status` - Current space weather status (Kp, solar wind, X-ray flux, etc.)
- `GET /health` - Health check

## Configuration

The server uses JSON structured logging by default, compatible with Grafana Loki.

### JVM Options

IPv4 is preferred by default (configured in build.sbt) to avoid IPv6 routing issues with some CDN endpoints.

## Project Structure

```
src/main/scala/com/solarion/
├── app/           # Application entry point
├── domain/        # Domain models and errors
├── routes/        # HTTP route definitions
└── services/      # Business logic and external API clients
```
