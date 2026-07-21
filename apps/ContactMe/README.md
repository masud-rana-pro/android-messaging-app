# ContactMe Android App

This is the main Android client for ContactMe. It is a Kotlin + Jetpack Compose app backed by Firebase Auth, Firestore, Realtime Database, Storage, FCM, Cloudinary media upload, native WebRTC calling, and Cloudflare Worker notification fanout.

## Main Features

- Email registration, login, password reset, profile setup, and session restore
- Realtime direct and group chat with Firestore listeners
- Text, image, document, and voice messages
- Message reply, edit, delete, read receipt, typing indicator, block, report, and unblock flows
- App-user discovery by username/phone and phone-contact matching
- Profile photo privacy, last seen privacy, read receipt settings
- Online/last-seen presence
- One-to-one audio/video calls using WebRTC signaling in Firestore
- Incoming call notification, foreground call service, active call recovery
- Group call invitation cards that open Jitsi rooms
- Chat, call history, settings, start-chat, group-creation, auth, profile, and call screens

## Open In Android Studio

Open this folder:

```text
D:\my-projects\github-projects\android-projects\ContactMe\apps\ContactMe
```

## Build

```powershell
.\gradlew.bat assembleDebug
```

Run unit tests:

```powershell
.\gradlew.bat testDebugUnitTest
```

Build and test together:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest
```

If Kotlin daemon or Gradle cache locking causes trouble, stop Gradle/Java processes for this project and retry with:

```powershell
.\gradlew.bat --no-daemon --console=plain "-Dkotlin.compiler.execution.strategy=in-process" assembleDebug testDebugUnitTest
```

## Source Structure

```text
app/src/main/java/com/contactme/app/
  auth/          Firebase Auth and phone/email auth helpers
  call/          WebRTC engine, signaling, call screens, foreground service
  contact/       Saved contacts and phone-contact matching
  conversation/  Direct/group conversation list and creation
  media/         Cloudinary upload, image/document/voice queues
  message/       Chat message models and Firestore message repository
  notification/  FCM payload parsing, channels, renderer, token sync
  presence/      Online and last-seen state
  profile/       User profile, privacy settings, profile photo handling
  safety/        Block/report validation and persistence
  typing/        Typing indicator repository
  ui/            Compose app shell, view models, screens, theme
```

## Runtime Requirements

- Android 7.0+ (`minSdk 24`)
- Firebase project with Auth, Firestore, Realtime Database, Storage, and FCM
- Valid `google-services.json` in `app/`
- Network access to Firebase, Cloudinary, Cloudflare Worker, Jitsi, and TURN server
- Camera, microphone, notification, contacts, and media permissions as needed

## Calling Requirements

Real devices need a reachable TURN server for reliable WebRTC audio/video across different networks. Keep real TURN credentials out of Git. The sample file is:

```text
webrtc.properties.example
```

Use a local-only `webrtc.properties` or secure Gradle/environment injection for real credentials.

## Notification Requirements

The app stores device FCM tokens under:

```text
user_devices/{userId}/devices/{deviceId}
```

Message and call notifications depend on the Cloudflare Worker in `backend/cloudflare-worker`. If the Android code is updated but the Worker is not deployed, foreground realtime chat can still work, but background push notifications may not.

## Verification

Latest verified command:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest
```

Result: build successful.
