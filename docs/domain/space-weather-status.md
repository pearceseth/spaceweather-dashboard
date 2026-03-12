# SpaceWeatherStatus

Aggregated space weather status response model.

## What These Metrics Mean

**Kp Index**: A planetary index measuring geomagnetic disturbance on a 0-9 scale. Values ≥5 indicate geomagnetic storms. Higher Kp correlates with auroral activity extending to lower latitudes and potential impacts on power grids and satellites.

**G-Scale**: NOAA's geomagnetic storm scale (G0-G5) derived from Kp. G1 is minor (aurora visible at high latitudes), G5 is extreme (widespread power grid issues, satellite drag).

**Bz Component**: The north-south component of the interplanetary magnetic field (IMF). Negative Bz (southward) allows solar wind energy to couple into Earth's magnetosphere, driving geomagnetic storms. Sustained Bz < -10 nT often precedes significant activity.

**Bt**: Total interplanetary magnetic field strength. Higher values indicate stronger solar wind magnetic structures.

**Solar Wind Speed/Density**: Plasma characteristics of the solar wind. Typical speed is 300-400 km/s; high-speed streams exceed 600 km/s. Combined with southward Bz, elevated speed and density enhance storm intensity.

**X-ray Flux Class**: Solar flare classification based on peak X-ray flux in the 0.1-0.8 nm band. Classes are A, B, C, M, X (each 10x stronger). M and X class flares can cause radio blackouts and energetic particle events.

**Proton Flux**: High-energy proton intensity from solar energetic particle (SEP) events. Flux ≥10 pfu (particle flux units) at ≥10 MeV defines a solar radiation storm, hazardous to astronauts and polar aviation.

Sources: [NOAA Space Weather Scales](https://www.swpc.noaa.gov/noaa-scales-explanation), [NOAA Glossary](https://www.swpc.noaa.gov/content/space-weather-glossary)

## Fields

| Field | Type | Description |
|-------|------|-------------|
| `kp` | Option[Double] | Kp index (0-9 scale) |
| `kpSource` | Option[String] | Source of Kp reading |
| `gScale` | Option[String] | Geomagnetic storm scale (G0-G5) |
| `gScaleLabel` | Option[String] | Human-readable storm level |
| `bz` | Option[Double] | IMF Bz component (nT) |
| `bt` | Option[Double] | IMF total (nT) |
| `solarWindSpeed` | Option[Int] | Solar wind speed (km/s) |
| `solarWindDensity` | Option[Double] | Solar wind density (p/cm³) |
| `xrayFluxClass` | Option[String] | X-ray flux classification |
| `xrayFluxValue` | Option[Double] | X-ray flux value (W/m²) |
| `protonFlux` | Option[Double] | Proton flux (pfu) |
| `protonEventInProgress` | Option[Boolean] | Proton event indicator |
| `updatedAt` | Instant | Response timestamp |

## Validation Rules

- All fields except `updatedAt` are optional to allow partial failure
- `kp` should be in range 0-9
- `gScale` is derived from `kp` using standard NOAA scale

## Derivation

Data is aggregated from multiple upstream sources:
- Kp: NOAA SWPC primary, GFZ fallback
- Solar wind: NOAA SWPC plasma and mag endpoints
- X-ray/Proton: NOAA GOES satellite data

## Implementation

Source: [`SpaceWeatherStatus.scala`](../../backend/src/main/scala/com/solarion/domain/SpaceWeatherStatus.scala)
