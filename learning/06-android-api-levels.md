# Step 6: Android API Levels

এই note-এ ContactMe app-এর API level decision ব্যাখ্যা করা হলো।

## Current setup

File:

```text
apps/ContactMe/app/build.gradle.kts
```

Current values:

```kotlin
compileSdk = 36

defaultConfig {
    minSdk = 24
    targetSdk = 36
}
```

## আগে কী ছিল

আগে ছিল:

```kotlin
compileSdk = 35
targetSdk = 35
minSdk = 24
```

এটা technically ঠিক ছিল। Google Play policy অনুযায়ী new app/update-এর জন্য Android 15, API 35 target করা acceptable। কিন্তু তোমার local SDK-তে Android 16 API 36 installed আছে, তাই future-ready রাখার জন্য `compileSdk` এবং `targetSdk` 36 করা হয়েছে।

## `compileSdk` কী

```kotlin
compileSdk = 36
```

এটা বলে app কোন Android SDK দিয়ে compile হবে।

মানে:

- code compile করার সময় Android API 36 পর্যন্ত API reference পাওয়া যাবে।
- latest platform warning/error early ধরা যাবে।
- app automatically Android 16-only হয়ে যায় না।

Important:

`compileSdk` বেশি হলেও app পুরনো Android-এ চলতে পারে, যদি `minSdk` কম থাকে এবং incompatible API direct ব্যবহার না করা হয়।

## `targetSdk` কী

```kotlin
targetSdk = 36
```

এটা বলে app কোন Android version-এর behavior target করছে।

মানে:

- Android system ধরে নেয় app latest behavior handle করতে প্রস্তুত।
- permission/privacy/background behavior latest Android অনুযায়ী apply হতে পারে।
- Play Store policy meet করতে এটা important।

কেন 36:

- Android 16 API 36 stable SDK installed আছে।
- project এখন early stage, তাই latest target নিয়ে শুরু করলে future migration কম হবে।
- messaging app future privacy/notification/security behavior early ধরতে পারবে।

## `minSdk` কী

```kotlin
minSdk = 24
```

এটা বলে app minimum কোন Android version support করবে।

API 24 মানে Android 7.0 Nougat।

কেন 24 রাখা হয়েছে:

- পুরনো device support ভালো থাকে।
- Firebase/Compose modern stack-এর জন্য practical lower bound।
- Bangladesh/portfolio/testing context-এ বেশি device cover করা যাবে।

`minSdk` 26 বা 28 করলে কিছু API সহজ হতে পারে, কিন্তু অনেক পুরনো device বাদ যাবে। এখন messaging app-এর জন্য `minSdk = 24` ভালো balance।

## Final decision

```text
compileSdk: 36
targetSdk: 36
minSdk: 24
```

এই combination মানে:

- latest Android SDK দিয়ে app build হবে
- latest Android behavior target করবে
- Android 7.0+ device support থাকবে

## Verification

Command:

```powershell
cd apps\ContactMe
.\gradlew.bat assembleDebug
```

Result:

```text
BUILD SUCCESSFUL
```

## মনে রাখার নিয়ম

- `compileSdk`: কোন SDK দিয়ে build হচ্ছে
- `targetSdk`: কোন Android behavior app target করছে
- `minSdk`: কত পুরনো Android device support করবে

Rule of thumb:

- `compileSdk` latest stable রাখো
- `targetSdk` Play policy/current stable অনুযায়ী latest রাখো
- `minSdk` user/device support বিবেচনা করে set করো
