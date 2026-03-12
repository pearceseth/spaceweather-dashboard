# NoaaProtonFlux

Parser for NOAA GOES proton flux data.

## What is Proton Flux?

Solar energetic particle (SEP) events accelerate protons to high energies, measured by GOES satellites. The ≥10 MeV proton flux is the standard metric: when it exceeds 10 pfu (particle flux units), NOAA declares a solar radiation storm (S1 or higher). These events pose radiation hazards to astronauts, can damage satellite electronics, increase radiation exposure on polar flight routes, and cause polar cap absorption (HF radio blackouts at high latitudes).

Sources: [NOAA SWPC Proton Flux](https://www.swpc.noaa.gov/products/goes-proton-flux), [NOAA Solar Radiation Storm Scale](https://www.swpc.noaa.gov/noaa-scales-explanation)

## Downstream URL

`https://services.swpc.noaa.gov/json/goes/primary/integral-protons-1-day.json`

## Data Extracted

- Proton flux (pfu - particle flux units)
- Proton event in progress flag

## Response Format

```json
[
  {"time_tag": "...", "flux": 0.15, "energy": ">=10 MeV"},
  ...
]
```

## Normalization

- Extracts the most recent >=10 MeV band reading
- Determines if proton event threshold (10 pfu) is exceeded

## Implementation

Source: [`NoaaProtonFlux.scala`](../../backend/src/main/scala/com/solarion/domain/upstream/NoaaProtonFlux.scala)
