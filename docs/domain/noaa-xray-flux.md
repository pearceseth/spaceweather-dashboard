# NoaaXrayFlux

Parser for NOAA GOES X-ray flux data.

## What is X-ray Flux?

Solar flares emit intense bursts of X-rays measured by GOES satellites. The 0.1-0.8 nm (long-wave) band is used for flare classification: A, B, C, M, and X classes, each 10× more intense than the previous. C-class flares are common and minor; M-class can cause brief radio blackouts; X-class flares are the most powerful, capable of causing widespread HF radio disruption, GPS degradation, and triggering solar energetic particle events.

Sources: [NOAA SWPC X-ray Flux](https://www.swpc.noaa.gov/products/goes-x-ray-flux), [NOAA Solar Flare Classification](https://www.swpc.noaa.gov/phenomena/solar-flares-radio-blackouts)

## Downstream URL

`https://services.swpc.noaa.gov/json/goes/primary/xrays-7-day.json`

## Data Extracted

- X-ray flux class (A, B, C, M, X scale)
- X-ray flux value (W/m²)

## Response Format

```json
[
  {"time_tag": "...", "flux": 2.1e-7, "energy": "0.1-0.8nm"},
  ...
]
```

## Normalization

- Extracts the most recent 0.1-0.8nm band reading
- Converts flux value to classification (A, B, C, M, X)

## Implementation

Source: [`NoaaXrayFlux.scala`](../../backend/src/main/scala/com/solarion/domain/upstream/NoaaXrayFlux.scala)
