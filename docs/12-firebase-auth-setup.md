# Firebase Auth Setup

This document tracks Firebase Auth setup for `feature/auth-build`.

## Current Status

Firebase Auth SDK dependency has been added, and `FirebaseAuthRepository` has been created.

The app still uses `FakeAuthRepository` through Hilt binding until Firebase project config is added.

## Why Fake Binding Remains Active

The app does not currently have:

```text
apps/ContactMe/app/google-services.json
```

Without Firebase config, applying the Google Services Gradle plugin can fail the build. So the project keeps the fake repository active until config is available.

## Required Manual Firebase Steps

1. Create a Firebase project.
2. Add an Android app with package:

```text
com.contactme.app
```

3. Download `google-services.json`.
4. Place it at:

```text
apps/ContactMe/app/google-services.json
```

5. Decide whether to commit it.

Current `.gitignore` ignores `google-services.json`, so it will not be committed by default.

## Next Code Step After Config

1. Add Google Services Gradle plugin.
2. Apply plugin in app module.
3. Switch Hilt binding from `FakeAuthRepository` to `FirebaseAuthRepository`.
4. Add register flow.
5. Add session restore.

## Current Firebase Dependency

```kotlin
implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
implementation("com.google.firebase:firebase-auth-ktx")
```

## Current Repository Skeleton

```text
auth/FirebaseAuthRepository.kt
di/FirebaseModule.kt
```

`FirebaseAuthRepository` currently supports email/password sign-in only. Registration will be added in a later step.
