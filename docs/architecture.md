# Architecture

## ZIO Layer Composition

The application uses ZIO's dependency injection via ZLayers:

```
Main
├── Server (zio-http)
├── Routes
│   ├── HealthRoutes → HealthService
│   └── StatusRoutes → SpaceWeatherStatusService
└── Services
    ├── HealthServiceLive
    └── SpaceWeatherStatusServiceLive
        ├── SpaceWeatherClientLive → Client (HTTP)
        └── Cache[SpaceWeatherStatus]
```

## Aggregation Pattern

The `/api/status` endpoint aggregates data from multiple upstream sources:

1. **Fan-out**: Parallel requests to NOAA SWPC and GFZ Potsdam
2. **Partial failure**: Each field is independently nullable—if one source fails, remaining data is still returned
3. **Caching**: Results are cached to reduce upstream load (TTL: 60 seconds)

## Request Lifecycle

1. HTTP request received by zio-http
2. Route handler invokes service layer
3. Service checks cache; on miss, fans out to SpaceWeatherClient methods
4. Client fetches and parses upstream JSON
5. Service aggregates results into SpaceWeatherStatus
6. Response serialized as JSON

## Implementation

Source: [`Main.scala`](../backend/src/main/scala/com/solarion/app/Main.scala)
