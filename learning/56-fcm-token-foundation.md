# Step 56 - FCM Token Foundation

এই ধাপে আমরা ContactMe app-এ notification-এর real foundation শুরু করেছি।

## এই step-এ notification পাঠানো হয়েছে?

না।

এখন শুধু notification পাঠানোর জন্য দরকারি base তৈরি হয়েছে:

```text
FCM dependency
notification channels
FirebaseMessagingService
device token sync
Firestore rules
```

## FCM token কী?

FCM token হলো একটা device/app instance-এর address-এর মতো। Server বা Cloud Functions এই token দিয়ে ওই device-এ push notification পাঠাতে পারে।

## কোথায় token save হচ্ছে?

Firestore path:

```text
user_devices/{uid}/devices/{deviceId}
```

Document shape:

```text
token
platform: android
updatedAt
```

## কেন `user_devices` আলাদা collection?

এক user-এর একাধিক device থাকতে পারে:

- phone
- tablet
- future second phone

তাই `users/{uid}` document-এর ভিতরে single token রাখা ঠিক না। আলাদা subcollection scalable।

## Notification channels কেন দরকার?

Android 8+ এ notification দেখাতে channel দরকার।

আমরা তিনটা channel বানিয়েছি:

```text
messages
calls
system
```

পরের step-এ message/call notification এই channel ব্যবহার করবে।

## `ContactMeMessagingService` কী করে?

FCM token refresh হলে Firebase এই service call করে:

```kotlin
override fun onNewToken(token: String)
```

তারপর app নতুন token Firestore-এ sync করে।

## App startup token sync

`DeviceTokenSyncViewModel` signed-in user থাকলে current token sync করে।

এতে app open করলেই Firestore-এ latest token থাকে।

## Security rules

User শুধু নিজের token document manage করতে পারবে:

```text
user_devices/{userId}/devices/{deviceId}
```

Rule idea:

```text
request.auth.uid == userId
```

## কীভাবে verify করবে?

1. App run করো।
2. Signed-in user থাকো।
3. Firestore console open করো।
4. `user_devices` collection দেখো।
5. তোমার `uid` এর নিচে `devices` document আছে কিনা দেখো।
6. Document-এ `token`, `platform`, `updatedAt` আছে কিনা দেখো।

## শেখার বিষয়

Notification feature সরাসরি push দেখানো দিয়ে শুরু করা উচিত না। আগে device token reliable ভাবে save করতে হয়। Token না থাকলে Cloud Functions জানবেই না notification কোন device-এ পাঠাবে।
