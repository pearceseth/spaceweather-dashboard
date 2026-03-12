# Health Endpoint

## Endpoint

```
GET /health
```

## Parameters

None.

## Response

```json
{
  "status": "ok",
  "timestamp": "2024-01-15T12:00:00Z"
}
```

### Fields

| Field | Type | Description |
|-------|------|-------------|
| `status` | string | Always "ok" when service is healthy |
| `timestamp` | string | ISO 8601 timestamp of response |

## Error Cases

This endpoint does not fail—it returns 200 OK if the service is running.

## Cache TTL

Not cached.

## Implementation

Source: [`HealthRoutes.scala`](../../backend/src/main/scala/com/solarion/routes/HealthRoutes.scala)
