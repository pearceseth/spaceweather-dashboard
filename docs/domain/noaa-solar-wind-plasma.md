# NoaaSolarWindPlasma

Parser for NOAA SWPC solar wind plasma data.

## What is Solar Wind Plasma?

Solar wind is a continuous stream of charged particles (mostly protons and electrons) flowing outward from the Sun at 300-800 km/s. Plasma measurements include speed and density, which determine the dynamic pressure exerted on Earth's magnetosphere. High-speed solar wind streams (>600 km/s) from coronal holes or coronal mass ejections can compress the magnetosphere and trigger geomagnetic storms when combined with southward IMF.

Sources: [NOAA SWPC Real-Time Solar Wind](https://www.swpc.noaa.gov/products/real-time-solar-wind), [NASA Solar Wind Overview](https://science.nasa.gov/heliophysics/focus-areas/solar-wind)

## Downstream URL

`https://services.swpc.noaa.gov/products/solar-wind/plasma-7-day.json`

## Data Extracted

- Solar wind speed (km/s)
- Solar wind density (p/cm³)

## Response Format

Array of arrays with plasma measurements.

## Normalization

Returns the most recent valid speed and density readings.

## Implementation

Source: [`NoaaSolarWindPlasma.scala`](../../backend/src/main/scala/com/solarion/domain/upstream/NoaaSolarWindPlasma.scala)
