# Step 1: Android Project Scaffold

এই ধাপে `apps/ContactMe` folder-এর ভিতরে ContactMe Android app-এর প্রাথমিক কাঠামো তৈরি করা হয়েছে।

## কী কাজ করা হয়েছে

- Android Gradle project তৈরি করা হয়েছে।
- `app` module তৈরি করা হয়েছে।
- Kotlin এবং Jetpack Compose setup করা হয়েছে।
- `MainActivity.kt` তৈরি করা হয়েছে।
- ContactMe theme color `#a605e6` দিয়ে basic Material 3 theme তৈরি করা হয়েছে।
- একটি simple chat-list style UI demo তৈরি করা হয়েছে।
- App name, manifest, theme, icon resource এবং Gradle configuration যোগ করা হয়েছে।

## কেন করা হয়েছে

যেকোনো বড় Android app শুরু করার আগে base project scaffold দরকার। এই scaffold ভবিষ্যতের সব feature-এর foundation:

- Auth screen
- Home tabs
- Chat list
- One-to-one chat
- Firebase integration
- Notification
- Calls
- Groups

প্রথমে UI demo project buildable করা হলে পরের feature যোগ করা সহজ হয়।

## কোন file-এর কী কাজ

### `apps/android/settings.gradle.kts`

এই file বলে project-এর নাম কী এবং কোন module build হবে। এখানে `:app` module include করা হয়েছে।

### `apps/android/build.gradle.kts`

এই root Gradle file-এ Android, Kotlin এবং Compose plugin version define করা হয়েছে।

### `apps/android/app/build.gradle.kts`

এটা main app module-এর build configuration। এখানে package name, SDK version, app version, Compose enable করা এবং dependency list দেওয়া হয়েছে।

### `apps/android/app/src/main/AndroidManifest.xml`

Android system এই file থেকে app-এর entry point জানে। এখানে `MainActivity` launcher activity হিসেবে set করা হয়েছে।

### `apps/android/app/src/main/java/com/contactme/app/MainActivity.kt`

এটাই app-এর প্রথম screen চালু করে। এখন এখানে demo ContactMe chat list UI আছে।

### `apps/android/app/src/main/java/com/contactme/app/ui/theme/Color.kt`

ContactMe app-এর color values রাখা হয়েছে।

### `apps/android/app/src/main/java/com/contactme/app/ui/theme/Theme.kt`

Material 3 theme setup করা হয়েছে, যাতে পুরো app একই color system follow করে।

## কীভাবে verify করবে

### File structure check

Project root থেকে চালাও:

```powershell
Get-ChildItem apps\android -Recurse
```

Expected: `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `MainActivity.kt`, `Theme.kt`, `Color.kt` দেখা যাবে।

### Android Studio দিয়ে verify

1. Android Studio open করো।
2. `D:\my-projects\github-projects\android-projects\ContactMe\apps\android` folder open করো।
3. Gradle sync complete হতে দাও।
4. Run button চাপো।

Expected output:

- App install হবে।
- Screen-এ `ContactMe` title দেখা যাবে।
- নিচে demo chat items দেখা যাবে:
  - `Ayesha Rahman`
  - `Team ContactMe`
  - `Design Notes`

## এই ধাপে কী শেখা হলো

- Android project সাধারণত Gradle দিয়ে build হয়।
- `settings.gradle.kts` project/module define করে।
- `build.gradle.kts` dependency ও build behavior control করে।
- `MainActivity` হলো app-এর entry point।
- Jetpack Compose XML layout ছাড়াই Kotlin code দিয়ে UI বানায়।
- Theme আলাদা রাখলে app-এর design consistency maintain করা সহজ হয়।

## এই ধাপের limitation

এই মেশিনে command line `gradle` পাওয়া যায়নি, তাই terminal থেকে build verify করা যায়নি। Android Studio দিয়ে project open করলে Gradle sync/build verify করা যাবে।

## পরের step

পরের implementation step হবে app navigation structure তৈরি করা:

- Splash screen
- Auth screens placeholder
- Home screen tabs
- Chats, Status, Calls, Communities, Channels placeholder
