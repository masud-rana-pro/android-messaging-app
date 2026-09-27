# ContactMe

ContactMe is a native Android messaging application built around realtime conversations, media sharing, presence, push notifications, and calling. It is designed as a modern, privacy-aware communication experience for Android devices.

## Highlights

- Email authentication, password reset, profile setup, and session restoration
- Realtime direct and group conversations
- Text, image, document, and voice messages
- Message replies, editing, deletion, read receipts, typing indicators, blocking, and reporting
- App-user discovery and device-contact matching
- Online status, last seen, and privacy controls
- One-to-one audio and video calls with WebRTC and TURN
- Incoming-call and message notifications through Firebase Cloud Messaging
- Group call invitations using Jitsi Meet rooms
- Light and dark Compose UI with accessible call, chat, settings, and profile flows

## Technology

| Area | Stack |
| --- | --- |
| Android client | Kotlin, Jetpack Compose, Hilt, WorkManager |
| Authentication and data | Firebase Authentication, Cloud Firestore, Realtime Database, Storage |
| Notifications | Firebase Cloud Messaging, Cloudflare Worker |
| Media | Cloudinary |
| One-to-one calling | WebRTC with TURN |
| Group calling | Jitsi Meet |

## Repository Layout

```text
apps/ContactMe/              Android application
backend/cloudflare-worker/   Secure FCM notification worker
firebase/                    Firebase security rules and indexes
```

## Run Locally

1. Open `apps/ContactMe` in Android Studio.
2. Add your Firebase `google-services.json` to `apps/ContactMe/app/`.
3. Configure Firebase Auth, Firestore, Realtime Database, Storage, and FCM for the Android package.
4. Create a local `webrtc.properties` from `webrtc.properties.example` and supply valid TURN details for real-device calling.
5. Build and install the app.

```powershell
cd apps\ContactMe
.\gradlew.bat assembleDebug testDebugUnitTest
```

## Backend Services

Firebase manages authentication, data, online presence, and client notifications. The Cloudflare Worker sends verified FCM notifications without exposing Firebase service-account credentials to the Android client. Its deployment instructions are in [backend/cloudflare-worker/README.md](backend/cloudflare-worker/README.md).

## Security

This repository intentionally excludes Firebase configuration files, service-account credentials, TURN credentials, Cloudinary credentials, signing keys, build outputs, and local IDE files. Use local configuration and platform secret stores for all production values.

## License

See [LICENSE](LICENSE).
