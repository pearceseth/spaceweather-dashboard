# Status Endpoint

## Endpoint

```
GET /api/status
```

## Parameters

None.

## Response

```json
{
  "kp": 3.67,
  "kpSource": "NOAA",
  "gScale": "G0",
  "gScaleLabel": "No Storm",
  "bz": -2.5,
  "bt": 5.1,
  "solarWindSpeed": 425,
  "solarWindDensity": 3.2,
  "xrayFluxClass": "B2.1",
  "xrayFluxValue": 2.1e-7,
  "protonFlux": 0.15,
  "protonEventInProgress": false,
  "updatedAt": "2024-01-15T12:00:00Z"
}
```

### Fields

| Field | Type | Description |
|-------|------|-------------|
| `kp` | number? | Kp index (0-9 scale) |
| `kpSource` | string? | Source of Kp reading ("NOAA" or "GFZ") |
| `gScale` | string? | Geomagnetic storm scale (G0-G5) |
| `gScaleLabel` | string? | Human-readable storm level |
| `bz` | number? | Interplanetary magnetic field Bz component (nT) |
| `bt` | number? | Interplanetary magnetic field total (nT) |
| `solarWindSpeed` | number? | Solar wind speed (km/s) |
| `solarWindDensity` | number? | Solar wind density (p/cm³) |
| `xrayFluxClass` | string? | X-ray flux classification (A, B, C, M, X) |
| `xrayFluxValue` | number? | X-ray flux value (W/m²) |
| `protonFlux` | number? | Proton flux (pfu) |
| `protonEventInProgress` | boolean? | Whether a proton event is occurring |
| `updatedAt` | string | ISO 8601 timestamp |

All fields except `updatedAt` are nullable—if an upstream source fails, that field will be null.

## Error Cases

- Returns partial data if any upstream source fails
- Returns 500 if all sources fail

## Cache TTL

60 seconds.

## Implementation

Source: [`StatusRoutes.scala`](../../backend/src/main/scala/com/solarion/routes/StatusRoutes.scala)
