# Step 7: Android System Components Plan

এই ধাপে ContactMe app-এর জন্য Android system components plan করা হয়েছে।

Main document:

```text
docs/09-android-system-components.md
```

## কেন এই plan দরকার

Android app শুধু screen দিয়ে শেষ হয় না। কিছু feature Android OS-এর special component ছাড়া ঠিকমতো করা যায় না।

যেমন:

- Push notification-এর জন্য `FirebaseMessagingService`
- File share/attachment-এর জন্য `FileProvider`
- Active call-এর জন্য `Foreground Service`
- Retry/background task-এর জন্য `WorkManager`
- Offline cache-এর জন্য `Room`

কিন্তু এগুলো একসাথে শুরুতে add করা উচিত না। ভুল সময়ে add করলে app unnecessarily complex হয়ে যায়।

## Current phase decision

আমরা এখন `v0.1 UI Demo` phase-এ আছি।

এই phase-এ দরকার:

```text
Activity + Jetpack Compose
```

এখন দরকার নেই:

- ContentProvider
- BroadcastReceiver
- Foreground Service
- FirebaseMessagingService
- WorkManager
- Room

কারণ এখনো real auth, chat, media, call, notification implement হয়নি।

## Activity

`MainActivity` app-এর entry point।

এটা এখন শুধু:

- Compose UI attach করে
- `ContactMeTheme` apply করে
- `ContactMeApp` চালায়

Rule:

```text
MainActivity thin রাখো।
```

মানে Activity-এর ভিতরে chat/auth/upload/call logic রাখব না।

## ContentProvider vs FileProvider

Custom `ContentProvider` এখন দরকার নেই।

কিন্তু future media phase-এ `FileProvider` দরকার হতে পারে।

কেন:

- camera/gallery/file attachment share করতে
- অন্য app-কে secure temporary file URI দিতে
- raw file path expose না করতে

Rule:

```text
Media attachment শুরু হওয়ার আগে FileProvider add করব না।
```

## BroadcastReceiver

BroadcastReceiver খুব careful ভাবে use করতে হয়।

এই app-এ future use হতে পারে:

- call notification accept/reject action
- missed call action
- boot completed পরে scheduled sync restore, যদি later দরকার হয়

কিন্তু chat realtime করার জন্য BroadcastReceiver ব্যবহার করব না।

Chat-এর জন্য ভালো:

- Firestore listener
- Realtime Database presence
- FCM
- WorkManager retry

## Service

Normal background service avoid করা ভালো। Modern Android background service restrict করে।

কিন্তু `Foreground Service` লাগতে পারে:

- active voice call
- active video call
- ongoing call notification

Rule:

```text
User actively aware না থাকলে Foreground Service ব্যবহার করব না।
```

## FirebaseMessagingService

Notification phase-এ এটা লাগবে।

Use:

- FCM token receive
- token backend/user device record-এ sync
- foreground notification handle
- message/call notification show

Phase:

```text
v0.6 Notifications
```

## WorkManager

Background retry/scheduled work-এর জন্য WorkManager best।

Use:

- failed message retry
- failed media upload retry
- FCM token sync
- expired status cleanup
- future backup task

Phase:

```text
v0.5 Media থেকে দরকার হতে পারে
v2.0 Offline/Backup phase-এ strongly দরকার
```

## Room Database

Room local database।

Use:

- chat cache
- last message cache
- offline queue
- sync state

কিন্তু এখনই add করব না।

কারণ:

```text
Firestore message schema stable হওয়ার আগে Room schema বানালে পরে বারবার migration লাগবে।
```

## Notification Channels

Android 8+ এ notification channel দরকার।

Planned channels:

- messages
- groups
- calls
- channels
- status
- system

Phase:

```text
v0.6 Notifications
```

## Permission plan

Important permissions:

- `POST_NOTIFICATIONS`: notifications
- `CAMERA`: profile photo, video call
- `RECORD_AUDIO`: voice note, voice/video call
- `READ_CONTACTS`: only if native contact sync implement করি

Rule:

```text
Permission তখনই চাইব যখন feature user ব্যবহার করতে যাচ্ছে।
```

App open করেই সব permission চাইব না।

## এই ধাপে কী শেখা হলো

- সব Android component শুরুতেই add করা উচিত না।
- Feature দরকার হলে component add করতে হয়।
- `Activity` screen host।
- `FileProvider` secure file sharing।
- `BroadcastReceiver` OS/notification event handling।
- `Foreground Service` active ongoing user-visible task।
- `FirebaseMessagingService` push notification।
- `WorkManager` retry/background work।
- `Room` local offline database।

## Next implementation implication

পরের code step-গুলোতে আমরা roadmap follow করব:

1. UI Demo stable করা
2. ViewModel/Hilt foundation
3. Firebase Auth
4. Chat MVP
5. Media/FileProvider
6. Notifications/FCM service
7. Calling/Foreground service
