# Space Weather Dashboard — Backend API Route Plan

This document describes the backend aggregation routes, their downstream data sources,
merge/normalization strategy, and caching policy.

All downstream sources are free and require no API key unless noted.

---

## Cross-Cutting Concerns

- All downstream requests within a route are fired **in parallel** (`ZIO.collectAllPar`)
- All fields are **independently nullable** — partial upstream failure returns whatever succeeded
- The **DONKI API requires a free NASA API key** — register at https://api.nasa.gov
- Cache is **per-route, in-memory** with TTL configured per update frequency of upstream sources
- Never poll upstream sources faster than their published update frequency

---

## Routes

### `GET /api/status`

> Single call the frontend makes on page load and on a polling interval.
> Populates the header bar vitals: Kp, G-scale, Bz, solar wind speed, X-ray class, proton flux.

**Cache TTL:** 1 minute

#### Downstream Sources

| Source | URL | Data Used |
|--------|-----|-----------|
| NOAA SWPC — Planetary K-index | `https://services.swpc.noaa.gov/products/noaa-planetary-k-index.json` | Current Kp value, NOAA G-scale |
| NOAA SWPC — Solar Wind Plasma | `https://services.swpc.noaa.gov/products/solar-wind/plasma-1-day.json` | Solar wind speed (km/s), density (p/cc), temperature |
| NOAA SWPC — Solar Wind Mag | `https://services.swpc.noaa.gov/products/solar-wind/mag-1-day.json` | Bz (nT), Bt, Bx, By components |
| NOAA SWPC — GOES X-ray Flux | `https://services.swpc.noaa.gov/json/goes/primary/xrays-1-day.json` | Current X-ray flux value, flare class (A/B/C/M/X) |
| NOAA SWPC — Proton Flux | `https://services.swpc.noaa.gov/json/goes/primary/integral-protons-1-day.json` | Proton flux level, event threshold status |
| GFZ Potsdam — Estimated Kp | `https://kp.gfz-potsdam.de/app/json/?start=now-1h&end=now` | Real-time estimated Kp (finer resolution, updates every minute) |

#### Merge Logic

Fan out all six requests in parallel. Take the most recent data point from each.
Use GFZ as the authoritative current Kp (more up-to-date than NOAA's 3-hour finalized value).
Assemble into a single flat response. Every field independently nullable — if solar wind
is unavailable, still return Kp and X-ray data.

#### Response Shape

```json
{
  "kp": 3.7,
  "kpSource": "gfz-estimated",
  "gScale": "G1",
  "gScaleLabel": "Minor Storm",
  "bz": -4.1,
  "bt": 6.2,
  "solarWindSpeed": 478,
  "solarWindDensity": 8.4,
  "xrayFluxClass": "M2.1",
  "xrayFluxValue": 2.1e-5,
  "protonFlux": 1.2,
  "protonEventInProgress": false,
  "updatedAt": "2025-03-06T14:00:00Z"
}
```

---

### `GET /api/kp-history?hours=24`

> Powers the Kp bar chart on the Overview tab.
> Returns a time series of 3-hour Kp readings for the requested window.

**Cache TTL:** 3 hours (matches NOAA finalization cadence)

#### Downstream Sources

| Source | URL | Data Used |
|--------|-----|-----------|
| NOAA SWPC — Kp History | `https://services.swpc.noaa.gov/products/noaa-planetary-k-index.json` | 3-hourly finalized Kp readings, up to 30 days |
| GFZ Potsdam — Estimated Kp | `https://kp.gfz-potsdam.de/app/json/?start=now-3h&end=now` | Real-time estimated tip value for the current open 3-hour window |

#### Merge Logic

Slice NOAA history to requested hours (default 24h = 8 entries).
Drop NOAA's last entry (current open window — unfinalized, less accurate).
Append GFZ estimated value as the "now" tip.
Normalize Kp: clip to 0–9, round to 1 decimal (handles NOAA's thirds notation: 3.33 → 3.3).
Normalize timestamps to ISO 8601 UTC.

#### Response Shape

```json
{
  "readings": [
    { "time": "2025-03-06T00:00:00Z", "kp": 1.3, "estimated": false, "source": "noaa" },
    { "time": "2025-03-06T03:00:00Z", "kp": 2.0, "estimated": false, "source": "noaa" },
    { "time": "2025-03-06T21:00:00Z", "kp": 3.7, "estimated": true,  "source": "gfz"  }
  ]
}
```

---

### `GET /api/solar-wind/history?hours=24`

> Powers the solar wind speed and Bz time series charts on the Solar Wind tab.
> Both charts share the same time axis so the data is returned together.

**Cache TTL:** 1 minute (solar wind updates at 1-minute resolution from DSCOVR)

#### Downstream Sources

| Source | URL | Data Used |
|--------|-----|-----------|
| NOAA SWPC — Solar Wind Plasma (7-day) | `https://services.swpc.noaa.gov/products/solar-wind/plasma-7-day.json` | Speed (km/s), density (p/cc), temperature (K) per timestamp |
| NOAA SWPC — Solar Wind Mag (7-day) | `https://services.swpc.noaa.gov/products/solar-wind/mag-7-day.json` | Bx, By, Bz, Bt (nT) per timestamp |

#### Merge Logic

Both endpoints return parallel time series from the same satellite (DSCOVR at L1).
Timestamps should align closely but not perfectly — zip by nearest timestamp within a
1-minute tolerance. Drop rows where either source has a gap or `-9999` fill value
(NOAA's sentinel for missing data). Slice to requested hours.

#### Response Shape

```json
{
  "readings": [
    {
      "time": "2025-03-06T13:00:00Z",
      "speed": 478,
      "density": 8.4,
      "temperature": 112000,
      "bz": -4.1,
      "bt": 6.2,
      "bx": -1.3,
      "by": 4.8
    }
  ]
}
```

---

### `GET /api/aurora/forecast`

> Powers the aurora oval map and visibility metrics on the Aurora tab.
> Returns the OVATION model probability grid plus derived visibility latitude.

**Cache TTL:** 30 minutes (OVATION model update frequency)

#### Downstream Sources

| Source | URL | Data Used |
|--------|-----|-----------|
| NOAA SWPC — OVATION Aurora (North) | `https://services.swpc.noaa.gov/json/ovation_aurora_latest.json` | Lat/lon probability grid for Northern hemisphere |
| NOAA SWPC — OVATION Aurora (South) | `https://services.swpc.noaa.gov/products/noaa-ovation-aurora-latest.json` | Lat/lon probability grid for Southern hemisphere |
| NOAA SWPC — Planetary K-index | `https://services.swpc.noaa.gov/products/noaa-planetary-k-index.json` | Current Kp for equatorward boundary calculation |

#### Merge Logic

The OVATION response is a large coordinate grid (~64KB). Two options depending on
frontend needs:

- **Pass-through:** return the raw grid and let the frontend render it directly
  (more flexible, heavier payload)
- **Pre-compute boundary:** extract the equatorward 50% probability contour as a
  simplified lat/lon polyline (lighter payload, less flexible)

Attach current Kp and the derived equatorward visibility latitude
(`viewingLatitude ≈ 75 - (kp * 2.5)`, clipped to 50–75°).

#### Response Shape

```json
{
  "forecastTime": "2025-03-06T14:00:00Z",
  "kp": 3.7,
  "viewingLatitudeNorth": 65.8,
  "viewingLatitudeSouth": -65.8,
  "grid": [
    { "lat": 90, "lon": -180, "probability": 0.0 },
    { "lat": 65, "lon": -150, "probability": 0.85 }
  ]
}
```

---

### `GET /api/events/recent?limit=20`

> Powers the live event feed on the Overview tab and the full Events tab.
> Aggregates and normalizes events from multiple NASA and NOAA sources into a unified type.

**Cache TTL:** 5 minutes

#### Downstream Sources

| Source | URL | Data Used |
|--------|-----|-----------|
| NASA DONKI — CME List | `https://api.nasa.gov/DONKI/CME?startDate={7daysAgo}&endDate={today}&api_key={key}` | CME time, speed, direction, Earth-directed flag |
| NASA DONKI — Solar Flares | `https://api.nasa.gov/DONKI/FLR?startDate={7daysAgo}&endDate={today}&api_key={key}` | Flare class, peak time, active region number |
| NASA DONKI — Geomagnetic Storms | `https://api.nasa.gov/DONKI/GST?startDate={7daysAgo}&endDate={today}&api_key={key}` | Storm onset time, Kp index, duration |
| NASA DONKI — High Speed Streams | `https://api.nasa.gov/DONKI/HSS?startDate={7daysAgo}&endDate={today}&api_key={key}` | HSS event time, associated solar wind speed |
| NOAA SWPC — Alerts & Warnings | `https://services.swpc.noaa.gov/products/alerts.json` | Active watches, warnings, alerts as plain text |

> **Note:** NASA DONKI requires a free API key from https://api.nasa.gov

#### Merge Logic

Each DONKI event type has a completely different response shape. Normalize all into a
shared `SpaceWeatherEvent` type. Map DONKI flare class and CME speed to a 3-tier
severity (`low` / `medium` / `high`). Merge SWPC alerts as a separate event type.
Deduplicate where the same event appears in both DONKI and SWPC alerts (match on
event type + timestamp within 30-minute window). Sort all by time descending,
take `limit`.

#### Severity Mapping

| Event | Low | Medium | High |
|-------|-----|--------|------|
| Solar Flare | B/C class | M class | X class |
| CME | < 500 km/s | 500–1000 km/s | > 1000 km/s |
| Geomagnetic Storm | G1 | G2–G3 | G4–G5 |
| HSS | < 500 km/s | 500–600 km/s | > 600 km/s |

#### Response Shape

```json
{
  "events": [
    {
      "id": "flr-2025-03-06T12:07Z",
      "type": "SOLAR_FLARE",
      "severity": "medium",
      "time": "2025-03-06T12:07:00Z",
      "summary": "M4.3 Solar Flare — Active Region 3664",
      "detail": {
        "flareClass": "M4.3",
        "activeRegion": "3664",
        "peakTime": "2025-03-06T12:07:00Z"
      },
      "source": "donki",
      "sourceUrl": "https://kauai.ccmc.gsfc.nasa.gov/DONKI/view/FLR/..."
    }
  ]
}
```

---

### `GET /api/forecast/3day`

> Powers the 3-day forecast section on the Overview tab.
> Returns predicted max Kp and storm scale per day for the next 3 days.

**Cache TTL:** 6 hours

#### Downstream Sources

| Source | URL | Data Used |
|--------|-----|-----------|
| NOAA SWPC — 3-Day Forecast | `https://services.swpc.noaa.gov/products/3-day-forecast.json` | Daily max predicted Kp, storm scale, narrative summary |
| NOAA SWPC — 27-Day Outlook | `https://services.swpc.noaa.gov/products/27-day-outlook.json` | Extended Kp and flux predictions (optional, for future extended view) |

#### Merge Logic

The 3-day forecast is mostly a passthrough and reshape. Parse the NOAA response,
extract one entry per day, normalize to a consistent shape. The 27-day outlook is
optional — include it in the response but the frontend only renders the first 3 days
by default.

#### Response Shape

```json
{
  "days": [
    {
      "date": "2025-03-06",
      "label": "Today",
      "maxKp": 4.2,
      "stormScale": "G1",
      "summary": "Active conditions expected, isolated minor storm possible"
    },
    {
      "date": "2025-03-07",
      "label": "Tomorrow",
      "maxKp": 5.7,
      "stormScale": "G2",
      "summary": "CME arrival expected, moderate storm conditions likely"
    },
    {
      "date": "2025-03-08",
      "label": "Sat",
      "maxKp": 3.1,
      "stormScale": "G1",
      "summary": "Subsiding activity, minor storm conditions possible"
    }
  ]
}
```

---

### `GET /api/imagery/latest`

> Returns URLs for the latest solar imagery from multiple instruments.
> The backend does **not** proxy the images — it returns URLs only.
> The frontend fetches images directly from their source CDNs.

**Cache TTL:** 15 minutes

#### Downstream Sources

| Source | URL | Data Used |
|--------|-----|-----------|
| NASA Helioviewer API — SDO AIA 171Å | `https://api.helioviewer.org/v2/getClosestImage/?date={now}&sourceId=10` | Latest SDO extreme UV image URL (corona, ~1M°C plasma) |
| NASA Helioviewer API — SDO AIA 304Å | `https://api.helioviewer.org/v2/getClosestImage/?date={now}&sourceId=9` | Latest SDO chromosphere image URL |
| NASA Helioviewer API — SDO HMI Magnetogram | `https://api.helioviewer.org/v2/getClosestImage/?date={now}&sourceId=18` | Latest solar magnetic field map URL |
| NOAA SWPC — CCOR-1 Coronagraph | `https://services.swpc.noaa.gov/products/ccor1/` | Latest CCOR-1 JPEG URL (solar corona, CME detection) |
| NOAA SWPC — GOES SUVI 171Å | `https://services.swpc.noaa.gov/products/suvi-primary-171.json` | Latest GOES solar UV imager image URL |

#### Merge Logic

Each source returns metadata including the image URL and timestamp. Extract just the
URL, instrument name, wavelength label, and capture time. No image data is fetched
or proxied — this route is a lightweight metadata aggregator only.

#### Response Shape

```json
{
  "images": [
    {
      "instrument": "SDO AIA",
      "wavelength": "171Å",
      "label": "Extreme UV — Corona",
      "capturedAt": "2025-03-06T13:48:00Z",
      "url": "https://helioviewer.org/jp2/..."
    },
    {
      "instrument": "CCOR-1",
      "wavelength": "white-light",
      "label": "Coronagraph — CME Detection",
      "capturedAt": "2025-03-06T13:45:00Z",
      "url": "https://services.swpc.noaa.gov/products/ccor1/..."
    }
  ]
}
```

---

### `GET /api/history/search?start=&end=&type=&limit=`

> Historical event search for the Events History tab.
> Enables users to query space weather events across a date range.
> This is the primary use case for the NCEI SPOT API.

**Cache TTL:** 1 hour (historical data is immutable)

#### Downstream Sources

| Source | URL | Data Used |
|--------|-----|-----------|
| NCEI SPOT API | `https://www.ncei.noaa.gov/cloud-access/space-weather-portal/api/v1/` | Deep archive search — decades of satellite data |
| NASA DONKI — CME (historical) | `https://api.nasa.gov/DONKI/CME?startDate={start}&endDate={end}&api_key={key}` | CME events with rich metadata back to 2010 |
| NASA DONKI — Solar Flares (historical) | `https://api.nasa.gov/DONKI/FLR?startDate={start}&endDate={end}&api_key={key}` | Flare events back to 2010 |
| NASA DONKI — Geomagnetic Storms (historical) | `https://api.nasa.gov/DONKI/GST?startDate={start}&endDate={end}&api_key={key}` | Storm events back to 2010 |

#### Merge Logic

SPOT is the authoritative deep archive (data going back decades) but has less structured
metadata. DONKI has richer, well-structured event data but only back to ~2010. For
queries after 2010, prefer DONKI metadata and use SPOT for any supplementary archive
data. For queries before 2010, SPOT is the only source. Normalize into the same
`SpaceWeatherEvent` shape as `/api/events/recent`. Paginate via `limit` and `offset`.

#### Query Parameters

| Param | Type | Description |
|-------|------|-------------|
| `start` | ISO 8601 date | Start of search window |
| `end` | ISO 8601 date | End of search window (default: now) |
| `type` | string | Filter by event type: `CME`, `FLARE`, `STORM`, `HSS` |
| `limit` | int | Max results (default: 20, max: 100) |
| `offset` | int | Pagination offset |

#### Response Shape

```json
{
  "total": 142,
  "offset": 0,
  "limit": 20,
  "events": [
    {
      "id": "gst-2024-05-10T17:00Z",
      "type": "GEOMAGNETIC_STORM",
      "severity": "high",
      "time": "2024-05-10T17:00:00Z",
      "summary": "G5 Extreme Geomagnetic Storm — Gannon Storm",
      "detail": {
        "maxKp": 9.0,
        "stormScale": "G5",
        "duration": "18h"
      },
      "source": "donki"
    }
  ]
}
```

---

## Source Reference

| Source | Base URL | Key Required | Update Frequency |
|--------|----------|-------------|-----------------|
| NOAA SWPC Products | `https://services.swpc.noaa.gov/products/` | No | Varies (1 min – 3 hrs) |
| NOAA SWPC JSON | `https://services.swpc.noaa.gov/json/` | No | Varies |
| GFZ Potsdam Kp | `https://kp.gfz-potsdam.de/app/json/` | No | 1 minute |
| NASA DONKI | `https://api.nasa.gov/DONKI/` | Yes (free) | ~15 minutes |
| NASA Helioviewer | `https://api.helioviewer.org/v2/` | No | ~15 minutes |
| NCEI SPOT | `https://www.ncei.noaa.gov/cloud-access/space-weather-portal/api/v1/` | No | Archive |

---

## Notes

- **DONKI API key:** Register for free at https://api.nasa.gov. Use `DEMO_KEY` for
  development (limited to 30 req/hour). Store production key in environment config,
  never in source.

- **NOAA SWPC rate limits:** Not publicly documented but government infrastructure —
  cache aggressively and do not poll faster than the TTLs above.

- **SPOT API:** Brand new as of September 2025 and currently only contains CCOR-1
  coronagraph data. Additional datasets will be migrated in over time. Worth keeping
  the `/api/history/search` route flexible as SPOT matures. Consult the Swagger UI
  at `https://www.ncei.noaa.gov/cloud-access/space-weather-portal/api/v1/openapi/`
  for current endpoint spec.

- **`/api/status` upgrade path:** Consider upgrading to Server-Sent Events (SSE) or
  WebSocket in a future iteration so the frontend header bar updates in real-time
  without polling.

- **CCOR-1 imagery format:** Raw CCOR-1 data from NCEI is in FITS format (astronomy
  standard), which is not browser-renderable. Use the pre-processed JPEGs from SWPC
  (`services.swpc.noaa.gov/products/ccor1/`) for the imagery route instead.