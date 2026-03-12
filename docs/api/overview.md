# API Overview

The Space Weather Dashboard API provides two endpoints.

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/health` | Health check |
| GET | `/api/status` | Aggregated space weather status |

## Common Response Headers

All responses include:
- `Content-Type: application/json`

## Error Handling

The API returns partial data when upstream sources fail. Each field in the response is independently nullable.

## Endpoint Documentation

- [Health](./health.md)
- [Status](./status.md)
