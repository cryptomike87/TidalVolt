# TidalVolt — Electric Vehicle Intelligence Platform

Native Android (Kotlin + Jetpack Compose) electric-vehicle intelligence app. Initial target: **2026 BYD Yuan Pro (45.1 kWh Blade LFP battery)** on **BYD DiLink (Android 13)**. The architecture is intended to expand to other EV brands while preserving trip history, charging analytics, range estimation and EV cost comparisons.

---

## 1. Compatibility & Research Findings (BYD `autoservice`)

Before implementing direct BYD vehicle access, open-source DiLink tooling (such as community `BYDMate` / `byd-trip-stats` / `DiPlay` research) and Android Binder architecture were audited:

### What Is Publicly Verifiable
1. **`android.hardware.bydauto.*` OEM SDK & System Service (`autoservice`)**:
   - BYD DiLink firmware exposes vendor device managers (`BYDAutoStatisticDevice`, `BYDAutoBatteryDevice`, `BYDAutoEnergyDevice`, `BYDAutoChargingDevice`, `BYDAutoSpeedDevice`, `BYDAutoBodyworkDevice`) backed by the `autoservice` Binder system service (`android.os.ServiceManager.getService("autoservice")`).
   - Documented read-only SDK methods include `getElecPercentageValue()`, `getBatteryVoltageValue()`, `getChargingPower()`, `getChargerWorkState()`, `getPowerGenerationValue()`, `getCurrentSpeed()`, `getSingleBatteryMinVoltage()`, and `getSingleBatteryMaxVoltage()`.
2. **Android Automotive OS VHAL (`android.car.Car` / `CarPropertyManager`)**:
   - Standard AOSP `VehiclePropertyIds` (e.g., `EV_BATTERY_LEVEL = 291504905`, `EV_BATTERY_CAPACITY = 291504902`, `PERF_VEHICLE_SPEED = 291504647`, `PERF_ODOMETER = 291504644`).
3. **Authorized Local ADB Shell Telemetry (Read-Only)**:
   - On DiLink Android 13, local ADB debugging (`127.0.0.1:5555`) runs at shell `UID 2000` to inspect `cmd car_service get-property-value`, `dumpsys`, BYD `energydata/EC_database.db`, and filtered `logcat` snapshots cleanly without root.

### What Cannot Be Fabricated or Executed Without Vehicle-Specific Verification
- **Raw `IBinder.transact()` integer codes for `autoservice`**: BYD does not publish public AIDL definitions or transaction code tables across DiLink ROM builds. TidalVolt inspects `autoservice` availability, liveliness (`pingBinder`), and `getInterfaceDescriptor()`, and reads verified SDK/VHAL getters—**raw proprietary Binder transaction codes are never guessed**.
- **Signature-level Permissions on BYD Yuan Pro (Android 13)**: `android.permission.BYDAUTO_*` and `android.car.permission.*` are marked `signature|privileged` on most global/2026 DiLink firmware. Standard sideloaded APKs will receive `SecurityException` unless signed with the OEM platform key or paired with authorized ADB debugging / local Di+ bridge.
- **Zero Root Assumption & Zero Control Commands**: TidalVolt is strictly **read-only** and never requests root (`su`) or executes vehicle actuation/control commands.

---

## 2. Battery Calculations & Data Distinction

- **Measured vs. Estimated:** Actual BMS readings (`BmsData` fields) remain `null` (`--` in UI) when not supplied by a verified source. Calculations are explicitly tagged **ESTIMATED**:
  - **Estimated Remaining Energy (kWh):** `45.1 × (SOC / 100)` *(Nominal capacity estimate, not measured usable energy)*
  - **Estimated Battery Power (kW):** `(Voltage × Current) / 1000` *(Positive Current = Discharging, Negative Current = Charging / Regen)*
  - **Cell Imbalance (mV):** `(Max Cell V − Min Cell V) × 1000`
  - **Estimated Remaining Range (mi):** `Usable Energy / Recent Consumption Rate` *(Uses live recent vehicle history when available; falls back to BYD Yuan Pro baseline `45.1 kWh / 245 mi = 0.184 kWh/mi`)*
- **Mock BMS Simulator:** Emits clearly labeled `SIMULATED MOCK` values for offline development and testing.

---

## 3. Secure WebView Bridge (Hybrid Extension Support)

`VehicleBmsJsBridge` (`VehicleBmsBridge` TypeScript interface in `app/src/main/assets/web/vehicle-bms-bridge.ts`) exposes strictly 4 read-only methods:
- `connect(): Promise<boolean>`
- `disconnect(): Promise<void>`
- `getConnectionStatus(): Promise<string>`
- `getBmsData(): Promise<BmsData>`

Restricted solely to trusted local origins (`file:///android_asset/` and `https://appassets.androidplatform.net/`) with zero shell execution or Binder reflection exposed to JavaScript.

---

## 4. Build & Installation Instructions

### Build via GitHub Actions
This repository includes `.github/workflows/build-apk.yml`, which automatically runs:
```bash
gradle :app:testDebugUnitTest :app:assembleDebug --no-daemon --stacktrace
```
on JDK 21 and Gradle 9.3.1, uploading the signed `TidalVolt-app-debug` artifact.

### Install on 2026 BYD Yuan Pro (DiLink Android 13)
1. Download `app-debug.apk` from GitHub Actions artifacts (or install directly via DiLink file manager / ADB).
2. Grant **Precision GPS Location** when prompted for Range Map origin tracking.

### Enabling Authorized Local ADB Debugging on DiLink
1. Open **DiLink Settings -> System -> Version / About** and tap **Build Number** 7 times to enable Developer Options.
2. Enable **USB Debugging** and **Wireless / Network ADB Debugging** (TCP port `5555`).
3. If TCP `5555` is closed after a car reboot, connect once via USB from a laptop and run:
   ```bash
   adb tcpip 5555
   ```
4. Open TidalVolt -> **BMS & Miles** tab -> tap **Connect ADB** (`127.0.0.1:5555`) and check **"Always allow from this computer"** on the DiLink RSA prompt.

---

## 5. Vehicle Connection Troubleshooting

| UI Connection State | Meaning & Next Step |
| :--- | :--- |
| **Connected** | Verified BYD SDK, VHAL, ADB shell, or Mock Simulator is delivering valid BMS packets (`1–3s` cadence). |
| **Connecting** | Probing `autoservice` Binder and authorized ADB endpoint (`127.0.0.1:5555`). |
| **Permission required** | OEM `BYDAUTO_*` / `android.car.*` threw `SecurityException`. Tap **Connect ADB** or check Developer Debugging. |
| **Unsupported vehicle** | Non-BYD Android device detected (no `autoservice` Binder). Use **Mock Simulator: ON** for testing. |
| **Data unavailable** | Binder or ADB session active, waiting on readable BYD `energydata` / VHAL packets; controlled exponential backoff active. |
| **Disconnected** | Exceeded retry threshold or manually disconnected; tap **Scan DiLink Bus** or **Connect ADB** to retry. |

---

## 6. Vehicle-side Companion Connection (new)

The existing **Remote Companion** entry is now a vehicle-side gateway screen only. This build does not contain a phone companion client or a second APK.

### Included in this source update
- QR invitations generated in-app with ZXing.
- One-time 256-bit URL-safe pairing tokens with a 120-second expiry.
- A P-256 EC host keypair generated/stored by Android Keystore; only its public key and invitation signature are placed in the QR payload.
- Explicit owner approval for a joining device.
- WebRTC data-only peer connection and a reliable ordered `byd-telemetry` DataChannel.
- WebSocket signaling client for host registration, peer approval, SDP and ICE exchange.
- Read-only telemetry serialization sourced from the existing BMS repository.
- Optional GPS sharing (disabled by default) and configurable telemetry interval.
- Optional TURN ICE servers supplied by the signaling service.
- A minimal Node.js signaling service under `companion-signaling/`.

### Signaling service setup

1. Deploy `companion-signaling/` on a server with Node.js 20+.
2. Install dependencies with `npm install` and run `npm start`.
3. Put it behind a TLS reverse proxy and use its `wss://` endpoint in the app's **Companion Connection** screen.
4. Optional TURN fallback: configure `TURN_URL`, `TURN_USERNAME`, and `TURN_CREDENTIAL` on the signaling server. Use short-lived TURN credentials from your TURN provider.

The signaling service coordinates session setup only. Vehicle telemetry is sent over the WebRTC DataChannel once a real peer connects. The service stores invitation room state in memory; restarting it invalidates outstanding QR invitations.

### Important limitations in this stage
- The companion phone app is intentionally not included.
- Remote pairing cannot complete until a signaling service is deployed and configured and a compatible client implements the same version-1 QR/signaling protocol.
- The host public key is included in the invitation for client-side identity pinning; the future companion client must verify/pin it as part of its pairing handshake.
- The initial gateway supports one active peer connection at a time. The protocol can be extended for multiple peers.
- TURN relay credentials are not bundled in the APK. Without TURN configuration, some mobile/carrier networks may fail to establish a direct WebRTC route.
- Location sharing is off by default. Enable it explicitly in Companion Connection settings.
- Only verified BMS fields are serialized as measured values. Estimated range/energy remain separate fields, and simulated telemetry is flagged in the payload.

### Telemetry protocol

Messages are JSON objects with `type: "vehicle_telemetry"` and `protocolVersion: 1`. Unknown measurements are `null`. The `simulated`, `dataSource`, and `lastBmsUpdate` fields allow a future client to distinguish mock, measured, stale, and unavailable readings.


## 7. TidalVolt AI Assistant (new)

- Added a dedicated **TidalVolt AI** navigation screen with conversational UI and suggested vehicle questions.
- The assistant context is built from the verified `BmsDashboardSnapshot`, saved trip records, saved charging sessions, selected vehicle profile and configured energy rates.
- Mock BMS readings are excluded from measured vehicle context. Unknown or unavailable measurements remain unavailable.
- Includes a deterministic on-device insight fallback for battery SOC/SOH, charging estimates, range estimates, cell voltage snapshots, recorded costs and trip efficiency.
- Optional Gemini integration uses Firebase AI Logic (`gemini-3.8-flash`) when explicitly enabled and Firebase is configured. The user must opt in; the prompt sends summarized telemetry and the question, not GPS coordinates.
- Conversation history is kept in memory for the current app session and is not persisted.

### Enable Gemini cloud AI

1. Add the Android app to a Firebase project and include the matching `google-services.json` in `app/`.
2. Enable Firebase AI Logic with the Google AI backend. Configure App Check: the app uses the debug provider in debug builds and Play Integrity in release builds. Register the debug token shown in Logcat in Firebase App Check for local testing.
3. Open TidalVolt AI and explicitly enable **Gemini cloud AI**.
4. If Firebase is not configured or a request fails, the assistant falls back to local vehicle insights.

The assistant is informational and read-only. It does not control vehicle functions or replace professional diagnostics. Cloud Gemini requests use Firebase AI Logic with limited-use App Check tokens; configure a valid provider and register the app in Firebase before enabling cloud mode.
