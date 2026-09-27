# ContactMe Android Client

The ContactMe Android client is written in Kotlin with Jetpack Compose. It provides authentication, profile management, realtime messaging, media sharing, presence, push notifications, and calling in a single native application.

## Build

Open this folder in Android Studio, or run:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest
```

## Required Local Configuration

- Add the Firebase `google-services.json` file to `app/`.
- Configure Firebase Authentication, Firestore, Realtime Database, Storage, and FCM.
- Copy `webrtc.properties.example` to a local `webrtc.properties` and provide TURN configuration for reliable real-device calling across networks.
- Deploy the notification worker before testing background message and call notifications.

No credentials, service accounts, API tokens, or signing files belong in version control.

## Key Packages

```text
auth/          Authentication and session handling
call/          WebRTC signaling, call engine, and foreground service
conversation/  Direct and group conversation workflows
media/         Image, document, and voice-message uploads
notification/  FCM token sync and notification rendering
presence/      Online and last-seen status
profile/       Profiles and privacy preferences
ui/            Compose screens, view models, navigation, and theme
```
