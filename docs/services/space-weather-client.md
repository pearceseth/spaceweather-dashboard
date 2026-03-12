# SpaceWeatherClient

HTTP client for fetching upstream space weather data.

## Interface

```scala
trait SpaceWeatherClient:
  def fetchNoaaKpIndex: ZIO[Scope, FetchError, Option[(Double, String)]]
  def fetchNoaaSolarWindPlasma: ZIO[Scope, FetchError, Option[PlasmaData]]
  def fetchNoaaSolarWindMag: ZIO[Scope, FetchError, Option[MagData]]
  def fetchNoaaXrayFlux: ZIO[Scope, FetchError, Option[XrayData]]
  def fetchNoaaProtonFlux: ZIO[Scope, FetchError, Option[ProtonData]]
  def fetchGfzKp: ZIO[Scope, FetchError, Option[(Double, String)]]
```

## Downstream URLs

| Method | URL |
|--------|-----|
| `fetchNoaaKpIndex` | `https://services.swpc.noaa.gov/products/noaa-planetary-k-index.json` |
| `fetchNoaaSolarWindPlasma` | `https://services.swpc.noaa.gov/products/solar-wind/plasma-7-day.json` |
| `fetchNoaaSolarWindMag` | `https://services.swpc.noaa.gov/products/solar-wind/mag-7-day.json` |
| `fetchNoaaXrayFlux` | `https://services.swpc.noaa.gov/json/goes/primary/xrays-7-day.json` |
| `fetchNoaaProtonFlux` | `https://services.swpc.noaa.gov/json/goes/primary/integral-protons-1-day.json` |
| `fetchGfzKp` | `https://kp.gfz-potsdam.de/app/json/?start=...&end=...` |

## Error Handling

- NetworkError for HTTP failures
- ParseError for JSON parsing failures
- Logged via FetchOps.logFailures extension

## Rate Limits

No API keys required. Upstream APIs are public and have generous rate limits.

## Implementation

Source: [`SpaceWeatherClient.scala`](../../backend/src/main/scala/com/solarion/services/SpaceWeatherClient.scala)
