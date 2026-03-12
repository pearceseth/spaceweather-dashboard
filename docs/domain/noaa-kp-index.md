# NoaaKpIndex

Parser for NOAA SWPC Kp index data.

## What is the Kp Index?

The Kp index measures planetary geomagnetic activity on a 0-9 scale. NOAA SWPC provides estimated Kp values in near-real-time based on a network of magnetometers. Kp ≥5 indicates a geomagnetic storm (G1 or higher on NOAA's scale). The index is useful for predicting aurora visibility, satellite drag, and potential impacts on power infrastructure.

Sources: [NOAA SWPC Kp Index](https://www.swpc.noaa.gov/products/planetary-k-index), [NOAA Space Weather Scales](https://www.swpc.noaa.gov/noaa-scales-explanation)

## Downstream URL

`https://services.swpc.noaa.gov/products/noaa-planetary-k-index.json`

## Data Extracted

- Kp index value
- Timestamp

## Response Format

Array of arrays: `[["time_tag", "Kp", ...], ...]`

## Normalization

Returns the most recent Kp value and its timestamp.

## Implementation

Source: [`NoaaKpIndex.scala`](../../backend/src/main/scala/com/solarion/domain/upstream/NoaaKpIndex.scala)
