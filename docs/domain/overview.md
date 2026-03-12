# Domain Overview

Domain types are organized into three categories:

## Response Models

Case classes representing API responses:

- [SpaceWeatherStatus](./space-weather-status.md) - Main aggregated status response
- [HealthStatus](./health-status.md) - Health check response

## Error Types

- [DomainError](./domain-error.md) - Error hierarchy for the application

## Upstream Parsers

Objects that parse raw JSON from upstream APIs into typed data:

- [NoaaKpIndex](./noaa-kp-index.md) - NOAA Kp index parser
- [NoaaSolarWindPlasma](./noaa-solar-wind-plasma.md) - Solar wind plasma parser
- [NoaaSolarWindMag](./noaa-solar-wind-mag.md) - Solar wind magnetic field parser
- [NoaaXrayFlux](./noaa-xray-flux.md) - X-ray flux parser
- [NoaaProtonFlux](./noaa-proton-flux.md) - Proton flux parser
- [GfzKp](./gfz-kp.md) - GFZ Potsdam Kp parser
