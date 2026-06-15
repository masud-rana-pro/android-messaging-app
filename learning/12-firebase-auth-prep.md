# Step 12: Firebase Auth Preparation

এই ধাপে Firebase Auth config ready হওয়ার পর app-এ Google Services plugin apply করা হয়েছে এবং Auth repository binding fake থেকে real Firebase implementation-এ switch করা হয়েছে।

## কী কী করা হয়েছে

```text
apps/ContactMe/build.gradle.kts
apps/ContactMe/app/build.gradle.kts
apps/ContactMe/app/src/main/java/com/contactme/app/di/AuthModule.kt
docs/12-firebase-auth-setup.md
```

## Firebase config কোথায়

Firebase config file:

```text
apps/ContactMe/app/google-services.json
```

এই file Git-এ commit হচ্ছে না, কারণ `.gitignore`-এ ignore করা আছে।

কেন ignore:

- এটা environment-specific config
- public repo-তে রাখা ভালো practice না
- future-এ dev/staging/prod config আলাদা হতে পারে

## Package name verify

Firebase config-এর package name:

```text
com.contactme.app
```

App Gradle-এর `applicationId`-ও:

```kotlin
applicationId = "com.contactme.app"
```

দুইটা match করা জরুরি। না মিললে Firebase config ঠিকমতো কাজ করবে না।

## Google Services plugin

Project-level Gradle:

```kotlin
id("com.google.gms.google-services") version "4.4.4" apply false
```

App-level Gradle:

```kotlin
id("com.google.gms.google-services")
```

এই plugin `google-services.json` পড়ে Android resource generate করে।

## Firebase BOM

```kotlin
implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
```

BOM মানে Bill of Materials।

কাজ:

- Firebase library versions compatible রাখে
- প্রতিটা Firebase dependency-তে আলাদা version লিখতে হয় না

## Firebase Auth dependency

```kotlin
implementation("com.google.firebase:firebase-auth")
```

এটা Firebase Authentication SDK যোগ করে।

আগে অনেক project-এ `firebase-auth-ktx` ব্যবহার করা হতো। আমরা এখন `firebase-auth` ব্যবহার করছি, কারণ main module-ই forward-compatible path।

Important compatibility note:

Firebase BoM `34.14.1` try করলে `firebase-auth 24.1.0` আসে, যেটা Kotlin metadata `2.3.0` দিয়ে compiled। আমাদের project এখন Kotlin `2.0.21`, তাই build fail করে। এজন্য আপাতত Firebase BoM `33.8.0` রাখা হয়েছে।

Future-এ Kotlin/AGP upgrade করলে Firebase BoM আবার latest করা যাবে।

## Binding switch

আগে:

```text
AuthRepository -> FakeAuthRepository
```

এখন:

```text
AuthRepository -> FirebaseAuthRepository
```

Code:

```kotlin
@Binds
@Singleton
abstract fun bindAuthRepository(
    firebaseAuthRepository: FirebaseAuthRepository
): AuthRepository
```

মানে এখন `AuthViewModel` যখন `AuthRepository` চাইবে, Hilt তাকে Firebase implementation দেবে।

## `FirebaseAuthRepository`

Current কাজ:

```kotlin
firebaseAuth.signInWithEmailAndPassword(
    emailOrPhone.trim(),
    password
).await()
```

এটা Firebase email/password sign-in call করে।

`await()` coroutine-friendly করে Firebase Task result অপেক্ষা করে।

## এখন কীভাবে verify করবে

Build:

```powershell
cd apps\ContactMe
.\gradlew.bat assembleDebug
```

Expected:

```text
BUILD SUCCESSFUL
```

Manual:

1. Firebase Console-এ Email/Password sign-in enable করতে হবে।
2. Firebase Console-এ test user create করতে হবে, অথবা register flow add করার পর app থেকে create করতে হবে।
3. App Auth screen-এ valid email/password দিয়ে login try করতে হবে।

## এখনো কী বাকি

- Register flow
- Session restore
- Logout
- Firebase error mapping
- Firestore user profile save
- Phone/OTP auth

## এই ধাপে কী শেখা হলো

- Firebase config ready হলে Google Services plugin apply করা যায়।
- `google-services.json` app module folder-এ থাকে।
- Firebase BoM versions manage করে।
- Repository interface থাকলে fake থেকে Firebase implementation-এ switch করা সহজ।
- Hilt binding বদলালেই ViewModel-এর code change না করেও implementation বদলানো যায়।
