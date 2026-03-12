# Data Sources

Reference table of all upstream APIs used by this service.

| Source | Base URL | Key Required | Update Frequency |
|--------|----------|--------------|------------------|
| NOAA SWPC Kp Index | `https://services.swpc.noaa.gov/products/noaa-planetary-k-index.json` | No | 1 hour |
| NOAA SWPC Solar Wind Plasma | `https://services.swpc.noaa.gov/products/solar-wind/plasma-7-day.json` | No | 1 minute |
| NOAA SWPC Solar Wind Mag | `https://services.swpc.noaa.gov/products/solar-wind/mag-7-day.json` | No | 1 minute |
| NOAA SWPC X-ray Flux | `https://services.swpc.noaa.gov/json/goes/primary/xrays-7-day.json` | No | 1 minute |
| NOAA SWPC Proton Flux | `https://services.swpc.noaa.gov/json/goes/primary/integral-protons-1-day.json` | No | 5 minutes |
| GFZ Potsdam Kp | `https://kp.gfz-potsdam.de/app/json/?start=...&end=...` | No | 3 hours |

## Implementation

Source: [`SpaceWeatherClientLive.scala`](../backend/src/main/scala/com/solarion/services/SpaceWeatherClient.scala)
