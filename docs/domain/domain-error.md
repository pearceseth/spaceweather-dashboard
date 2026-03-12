# Domain Errors

Error hierarchy for the application.

## DomainError (sealed trait)

Base trait for all domain errors.

### Fields

| Field | Type | Description |
|-------|------|-------------|
| `message` | String | Error message |
| `context` | Option[String] | Additional context |

## ModelCreationError

Error during model creation/validation.

## DataSource (enum)

Identifies upstream data sources:
- `GfzKp`
- `NoaaKpIndex`
- `NoaaSolarWindPlasma`
- `NoaaSolarWindMag`
- `NoaaXrayFlux`
- `NoaaProtonFlux`

## FetchError (sealed trait)

Errors during upstream data fetching.

### Fields

| Field | Type | Description |
|-------|------|-------------|
| `dataSource` | DataSource | Which source failed |

### Subtypes

- **NetworkError**: HTTP/connection failures
- **ParseError**: JSON parsing failures

## Implementation

Source: [`DomainError.scala`](../../backend/src/main/scala/com/solarion/domain/error/DomainError.scala)
