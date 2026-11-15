# TidalVolt Companion

A separate, installable Android app in the same repository and Gradle root as TidalVolt.

- Android application ID: `com.tidalvolt.companion`
- Module: `:companion`
- Minimum Android: API 26
- Independent APK and app lifecycle
- Kept separate from the primary TidalVolt app so it can evolve as the vehicle/BMS connection layer.

## Current status

This is the app foundation and starter dashboard only. It does **not** yet connect to the BYD BMS, read vehicle data, or send telemetry to TidalVolt. The dashboard intentionally shows no invented readings.

## Planned responsibilities

1. Establish and maintain the supported BYD vehicle/BMS connection.
2. Normalize vehicle readings into a versioned telemetry contract.
3. Pair securely with TidalVolt and share only user-approved data.
4. Include source timestamps, units, connection state and stale-data indicators.
5. Queue/retry updates safely when either app is offline.

## Compatibility contract (planned)

The first inter-app contract will be versioned JSON. Proposed fields:

```json
{
  "schemaVersion": 1,
  "vehicle": { "make": "BYD", "model": "Yuan Pro" },
  "observedAt": "2026-01-01T12:00:00Z",
  "source": "companion",
  "connectionState": "connected",
  "telemetry": {
    "batteryPercent": null,
    "estimatedRangeMiles": null,
    "speedMph": null,
    "odometerMiles": null,
    "charging": null,
    "chargingPowerKw": null,
    "latitude": null,
    "longitude": null
  }
}
```

Null means unavailable; it must never be replaced with a fabricated value. Real implementation will add authentication, replay protection, explicit consent, field validation and a documented transport before either app exchanges live data.

## Build

From the repository root:

```bash
./gradlew :companion:assembleDebug
```

The existing TidalVolt app remains independently buildable with:

```bash
./gradlew :app:assembleDebug
```
