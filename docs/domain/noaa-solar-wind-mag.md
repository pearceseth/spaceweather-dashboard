# NoaaSolarWindMag

Parser for NOAA SWPC solar wind magnetic field data.

## What is the Interplanetary Magnetic Field?

The interplanetary magnetic field (IMF) is the Sun's magnetic field carried outward by the solar wind. The Bz component (north-south orientation in GSM coordinates) is critical for space weather: when Bz is negative (southward), it can reconnect with Earth's northward-pointing magnetic field, allowing solar wind energy to enter the magnetosphere. Sustained Bz < -10 nT typically drives significant geomagnetic activity. Bt represents the total field magnitude.

Sources: [NOAA SWPC IMF Data](https://www.swpc.noaa.gov/products/real-time-solar-wind), [SpaceWeatherLive IMF Explanation](https://www.spaceweatherlive.com/en/help/the-interplanetary-magnetic-field-imf)

## Downstream URL

`https://services.swpc.noaa.gov/products/solar-wind/mag-7-day.json`

## Data Extracted

- Bz component (nT) - north-south IMF component
- Bt total (nT) - total magnetic field strength

## Response Format

Array of arrays with magnetic field measurements.

## Normalization

Returns the most recent valid Bz and Bt readings.

## Implementation

Source: [`NoaaSolarWindMag.scala`](../../backend/src/main/scala/com/solarion/domain/upstream/NoaaSolarWindMag.scala)
