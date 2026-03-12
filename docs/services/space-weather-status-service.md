# SpaceWeatherStatusService

Aggregates space weather data from multiple upstream sources.

## Interface

```scala
trait SpaceWeatherStatusService:
  def getStatus: UIO[SpaceWeatherStatus]
```

## Aggregation Strategy

1. Fans out to all SpaceWeatherClient methods in parallel
2. Each fetch returns Option—failures become None
3. Aggregates all results into SpaceWeatherStatus
4. Caches result for 60 seconds

## Error Handling

- Never fails (returns UIO)
- Partial failures result in null fields in response
- Uses `.option` to convert FetchError to Option

## Caching

Uses ZIO Cache with 60-second TTL.

## Implementation

Source: [`SpaceWeatherStatusService.scala`](../../backend/src/main/scala/com/solarion/services/SpaceWeatherStatusService.scala)
