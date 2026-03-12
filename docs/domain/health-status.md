# HealthStatus

Health check response model.

## Fields

| Field | Type | Description |
|-------|------|-------------|
| `status` | String | Health status ("ok") |
| `timestamp` | Instant | Response timestamp |

## Validation Rules

- `status` is always "ok" when the service is running

## Implementation

Source: [`HealthStatus.scala`](../../backend/src/main/scala/com/solarion/domain/HealthStatus.scala)
