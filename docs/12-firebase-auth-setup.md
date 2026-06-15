# Firebase Auth Setup

This document tracks Firebase Auth setup for `feature/auth-build`.

## Current Status

Firebase config is available locally, the Google Services Gradle plugin is applied, Firebase Auth SDK is included, and Hilt now binds:

```text
AuthRepository -> FirebaseAuthRepository
```

## Firebase Config

Local config file:

```text
apps/ContactMe/app/google-services.json
```

This file is ignored by Git:

```text
google-services.json
```

## Package Name

Firebase Android app package:

```text
com.contactme.app
```

This matches the app `applicationId`.

## Gradle Plugin

Project-level Gradle:

```kotlin
id("com.google.gms.google-services") version "4.4.4" apply false
```

App-level Gradle:

```kotlin
id("com.google.gms.google-services")
```

The plugin processes `google-services.json` and generates Firebase resources for the app.

## Firebase Dependencies

```kotlin
implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
implementation("com.google.firebase:firebase-auth")
```

The project currently uses Firebase BoM `33.8.0` because the latest Firebase Auth artifact pulled by BoM `34.14.1` requires newer Kotlin metadata than this project currently uses.

Future cleanup:

- Upgrade Kotlin and Android Gradle Plugin together.
- Then upgrade Firebase BoM.

## Current Auth Repository

```text
auth/FirebaseAuthRepository.kt
```

Current support:

- Email/password sign-in
- Basic blank email check
- Basic password length check
- Firebase error message fallback

## Not Done Yet

- Email/password registration
- Session restore
- Logout
- Better Firebase error mapping
- Firestore user profile document save
- Phone/OTP auth

## Next Step

Add register flow to `AuthRepository`, `FirebaseAuthRepository`, `FakeAuthRepository`, and `AuthViewModel`.
