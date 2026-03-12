# GfzKp

Parser for GFZ Potsdam Kp index data.

## What is the Kp Index?

The Kp index is a global measure of geomagnetic activity derived from ground-based magnetometer stations. It quantifies disturbances in Earth's magnetic field caused by solar wind on a quasi-logarithmic scale from 0 (quiet) to 9 (extreme storm). GFZ Potsdam has calculated the official Kp index since 1997, providing both real-time estimates and definitive historical values.

Sources: [GFZ Potsdam Kp Index](https://kp.gfz-potsdam.de/en/), [IAGA Kp Definition](https://www.ngdc.noaa.gov/stp/GEOMAG/kp_ap.html)

## Downstream URL

`https://kp.gfz-potsdam.de/app/json/?start=...&end=...`

## Data Extracted

- Kp index value
- Timestamp

## Response Format

```json
{
  "datetime": ["2024-01-15 12:00:00", ...],
  "Kp": [3.67, ...]
}
```

## Normalization

Returns the most recent Kp value and its timestamp.

## Implementation

Source: [`GfzKp.scala`](../../backend/src/main/scala/com/solarion/domain/upstream/GfzKp.scala)
