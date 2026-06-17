# Step 57 - Notification Runtime Permission

এই ধাপে আমরা Android 13+ notification runtime permission request করেছি।

## কেন দরকার?

Android 13 থেকে notification দেখাতে user permission লাগে:

```text
POST_NOTIFICATIONS
```

Manifest-এ permission লিখলেই যথেষ্ট না। Runtime-এ user-এর কাছ থেকেও allow নিতে হয়।

## কোথায় implement করা হলো?

File:

```text
apps/ContactMe/app/src/main/java/com/contactme/app/MainActivity.kt
```

কারণ notification permission Activity-level OS permission।

## কীভাবে request করা হলো?

Activity Result API ব্যবহার করা হয়েছে:

```kotlin
registerForActivityResult(
    ActivityResultContracts.RequestPermission()
)
```

এটা modern Android permission request pattern।

## Android version check

Permission শুধু Android 13+ এ দরকার:

```kotlin
if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
```

পুরনো Android version-এ request করলে দরকার নেই।

## Permission না দিলে app বন্ধ হবে?

না।

Callback-এ আমরা app block করছি না:

```kotlin
// The app can still run without notification permission.
```

কারণ user notification deny করলেও chat/auth/profile কাজ করা উচিত।

## কীভাবে verify করবে?

1. Android 13+ emulator/device-এ app run করো।
2. System notification permission dialog আসা উচিত।
3. Deny করলে app খুলবে।
4. Allow করলে app খুলবে।
5. পরে notification feature এলে allow করা device notification পাবে।

## শেখার বিষয়

Android permission দুই জায়গায় লাগে:

```text
Manifest declaration
Runtime request
```

Manifest app capability declare করে। Runtime request user consent নেয়।
