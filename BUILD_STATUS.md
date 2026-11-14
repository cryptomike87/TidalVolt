# Companion gateway source update status

- Existing vehicle application source has been updated with a vehicle-side companion gateway.
- No second Android application or companion APK has been created.
- Added QR invitation generation, expiring one-time tokens, Android Keystore EC key generation/signing, signaling client, WebRTC DataChannel host, read-only telemetry serialization, and a Node.js signaling service.
- Removed the previous simulated companion-phone mode, fixed pairing PIN/IP defaults, and simulated remote climate/lock/honk actions from the active Companion Connection screen.
- Added WSS endpoint configuration; plain WS is accepted only for localhost development.
- Location sharing is disabled by default.
- Node signaling source passed `node --check`; package metadata passed JSON parsing; Kotlin source passed a parser-only syntax check.
- A full Android Gradle build and unit-test run were not possible in the editing environment because it does not contain Gradle, the Android SDK, or the project's resolved Android dependencies. Run the repository's GitHub Actions workflow or `gradle :app:testDebugUnitTest :app:assembleDebug` in a configured Android build environment before installing.

## Still required before real remote pairing

1. Deploy `companion-signaling/` behind TLS and configure its `wss://` URL in the vehicle app.
2. Configure TURN credentials on the signaling server for networks that need relay fallback.
3. Build the future companion client against the documented version-1 QR and signaling protocol. That client is intentionally not part of this update.
4. Test on the actual DiLink head unit and confirm that the vehicle firmware permits the app to run and maintain network access as required.


## TidalVolt brand + AI assistant update

- Rebranded visible application name and primary headers to **TidalVolt** while preserving the Android application ID and Kotlin package names to avoid breaking existing installs and integrations.
- Updated the launcher foreground artwork to a minimalist wave-and-bolt mark.
- Added a dedicated **TidalVolt AI** navigation destination and chat interface.
- Added a vehicle-context builder using verified `BmsDashboardSnapshot` measurements, saved Room trip records, saved charging sessions, vehicle profile and configured energy rates.
- Explicitly excludes simulated BMS measurements from the assistant's measured-data context.
- Added a deterministic on-device insight fallback and optional opt-in Firebase AI Logic/Gemini integration.
- Added unit tests for SOC truthfulness, simulated-data exclusion, charging-estimate assumptions and unavailable SOH handling.
- Gemini cloud mode is disabled by default and requires a Firebase project configuration (`google-services.json`) plus Firebase AI Logic and App Check setup.

- Added Firebase App Check initialization for configured Firebase projects: Debug provider in debug builds and Play Integrity provider in release builds. Gemini requests use limited-use App Check tokens.
- Cloud AI is opt-in; GPS coordinates are not included in the AI prompt.
- Updated Firebase Android BoM to 35.2.0 and Google Services Gradle plugin to 4.6.0, matching current Firebase Android release guidance as of 2026-11-14.
- The existing Android application ID and Kotlin package names remain unchanged to preserve update compatibility.

- Local assistant engine smoke tests passed in this editing environment (six checks covering measured SOC, mock-data exclusion, charging time, unavailable SOH, charging-cost estimates and range-improvement guidance).
- XML/JSON validation and signaling-server `node --check` passed. A full Android/Compose/Gradle build is still pending because Gradle and the Android SDK are not installed in this environment.
- No new APK was produced; the current project ZIP is source-only plus the original legacy artifacts.
