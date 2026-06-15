# Step 12: Firebase Auth Preparation

এই ধাপে real Firebase Auth শুরু করার প্রস্তুতি নেওয়া হয়েছে, কিন্তু app এখনো fake auth binding ব্যবহার করছে।

## কেন fake binding রাখা হয়েছে

Firebase Auth fully চালাতে `google-services.json` দরকার।

এখন project-এ এই file নেই:

```text
apps/ContactMe/app/google-services.json
```

তাই Google Services plugin apply করলে build fail হতে পারে। এজন্য Firebase SDK dependency এবং repository skeleton যোগ করা হয়েছে, কিন্তু active binding এখনো fake।

## কী কী যোগ হয়েছে

```text
auth/FirebaseAuthRepository.kt
di/FirebaseModule.kt
docs/12-firebase-auth-setup.md
```

Update হয়েছে:

```text
apps/ContactMe/app/build.gradle.kts
```

## Firebase BOM

```kotlin
implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
```

BOM মানে Bill of Materials।

কেন দরকার:

- Firebase libraries-এর compatible version manage করে।
- আলাদা Firebase dependency-তে version লিখতে হয় না।

## Firebase Auth dependency

```kotlin
implementation("com.google.firebase:firebase-auth-ktx")
```

এটা Firebase Authentication SDK যোগ করে।

## `FirebaseModule`

```kotlin
@Provides
@Singleton
fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
```

এটা Hilt-কে বলে কীভাবে `FirebaseAuth` instance তৈরি করতে হবে।

## `FirebaseAuthRepository`

```kotlin
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository
```

এই repository `AuthRepository` implement করে।

এখন এতে email/password sign-in skeleton আছে:

```kotlin
firebaseAuth.signInWithEmailAndPassword(
    emailOrPhone.trim(),
    password
).await()
```

`await()` coroutine-friendly Firebase call করে।

## কেন এখনই binding switch করা হয়নি

বর্তমানে `AuthModule` এখনো বলে:

```text
AuthRepository -> FakeAuthRepository
```

কারণ Firebase config নেই।

Config যোগ করার পর switch হবে:

```text
AuthRepository -> FirebaseAuthRepository
```

## তোমার manual কাজ

Firebase Console-এ:

1. New Firebase project create করো।
2. Android app add করো।
3. Package name দাও:

```text
com.contactme.app
```

4. `google-services.json` download করো।
5. রাখো:

```text
apps/ContactMe/app/google-services.json
```

## এই ধাপে কী শেখা হলো

- Firebase SDK dependency add করা আর Firebase config apply করা আলাদা কাজ।
- `google-services.json` ছাড়া Firebase plugin fully apply করা উচিত না।
- Repository interface থাকলে fake থেকে Firebase-এ switch করা সহজ।
- Hilt module দিয়ে Firebase SDK instance provide করা যায়।

## পরের step

Firebase config file ready হলে:

- Google Services plugin add/apply
- Auth binding switch
- real login
- register flow
- session restore
