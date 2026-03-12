# Space Weather Dashboard API

A Scala/ZIO backend API that aggregates space weather data from multiple upstream sources.

## Quick Start

```bash
cd backend
sbt run
```

The server starts on `http://localhost:8080`.

## API Endpoints

- `GET /health` - Health check endpoint
- `GET /api/status` - Aggregated space weather status

## Documentation

- [Architecture](./architecture.md) - System design and ZIO layer composition
- [Data Sources](./data-sources.md) - Upstream API reference table
- [API Overview](./api/overview.md) - API endpoint documentation

## Implementation

Source: [`Main.scala`](../backend/src/main/scala/com/solarion/app/Main.scala)
