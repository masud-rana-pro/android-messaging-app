# ContactMe

ContactMe is an Android-first messaging app project inspired by modern messenger workflows. The project is being built step by step from a UI demo toward auth, realtime chat, media, notifications, calling, groups, status, channels, privacy, backup, and admin tooling.

## Current Phase

```text
v0.1 UI Demo
```

The current app is a non-backend UI prototype. It validates the main user journey before Firebase Auth and realtime chat are implemented.

## Current Demo Flow

```text
Splash
-> Auth
-> Profile Setup
-> Home
-> Chat Detail
-> Profile & Settings
```

## Android App Location

Open this folder in Android Studio:

```text
apps/ContactMe
```

## Build

From the Android project folder:

```powershell
cd apps\ContactMe
.\gradlew.bat assembleDebug
```

Expected:

```text
BUILD SUCCESSFUL
```

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Gradle Kotlin DSL
- Planned: Hilt, ViewModel, Coroutines, Flow
- Planned: Firebase Auth, Firestore, Realtime Database, Storage, FCM
- Planned: ZegoCloud for calls

## Android SDK Levels

```text
compileSdk: 36
targetSdk: 36
minSdk: 24
```

`minSdk = 24` means Android 7.0+ devices are supported.

## Documentation

- Full roadmap: `docs/ContactMe_Full_App_Roadmap.docx`
- Architecture: `docs/03-architecture.md`
- Database plan: `docs/04-database-schema.md`
- Security plan: `docs/05-security-rules.md`
- Android system components: `docs/09-android-system-components.md`
- v0.1 verification: `docs/10-v01-ui-demo-verification.md`
- Learning notes: `learning/`

## Current Limitations

- No real Firebase Auth yet.
- No real profile persistence yet.
- No real contacts sync/search yet.
- No real chat send/receive yet.
- No media upload yet.
- No notification/calling implementation yet.

These limitations are expected for `v0.1 UI Demo`.
