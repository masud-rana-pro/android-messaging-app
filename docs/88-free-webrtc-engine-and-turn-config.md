# Step 88: Free WebRTC Engine and Secure TURN Config

## Implementation

- Added the Maven Central WebRTC Android SDK; no ZEGOCLOUD SDK/config remains.
- Added microphone, camera, audio-routing, and Bluetooth permissions.
- Added a reusable `PeerConnectionFactory` with audio/video encoder/decoder support.
- Added Unified Plan and continual ICE gathering configuration.
- Always configures `stun:stun.l.google.com:19302`.
- Adds Metered.ca TURN only when all local values are non-empty.
- Local TURN values are loaded from ignored `apps/ContactMe/webrtc.properties` into BuildConfig.
- A credential-free `webrtc.properties.example` documents the keys.

## Security Boundary

No real TURN username/password is committed or hardcoded in Kotlin. BuildConfig prevents source-control leakage but does not make static APK credentials secret. Production-grade short-lived TURN credentials would require a trusted future endpoint; this no-card checkpoint uses local Metered.ca credentials for personal beta only.

## Verification

`git diff --check` and targeted dependency/config review only. Gradle dependency resolution/build was skipped by instruction.
