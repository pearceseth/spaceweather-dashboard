# HealthService

Simple health check service.

## Interface

```scala
trait HealthService:
  def check: UIO[HealthStatus]
```

## Downstream URLs

None - no external calls.

## Error Handling

Cannot fail (returns UIO).

## Implementation

Source: [`HealthService.scala`](../../backend/src/main/scala/com/solarion/services/HealthService.scala)
