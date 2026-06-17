# Step 58 - FCM Notification Renderer

এই ধাপে আমরা FCM message এলে Android notification দেখানোর renderer যোগ করেছি।

## আগের step-এ কী ছিল?

আগের step-এ app FCM token save করছিল এবং notification channels তৈরি করছিল। কিন্তু FCM message এলে notification দেখানোর code ছিল না।

এই step-এ সেই display foundation যোগ হলো।

## Main class

নতুন file:

```text
apps/ContactMe/app/src/main/java/com/contactme/app/notification/ContactMeNotificationRenderer.kt
```

এর কাজ:

```text
RemoteMessage -> payload parse -> channel choose -> Android notification show
```

## Service কীভাবে use করছে?

`ContactMeMessagingService`-এ:

```kotlin
override fun onMessageReceived(message: RemoteMessage) {
    notificationRenderer.show(message)
}
```

মানে Firebase থেকে message এলে renderer notification বানাবে।

## Payload fields

Renderer data payload থেকে এগুলো পড়ে:

```text
type
conversationId
title
body
```

Example:

```json
{
  "type": "message",
  "conversationId": "direct_uidA_uidB",
  "title": "Masud Rana",
  "body": "New message"
}
```

## Channel select

```text
type = call -> calls channel
type = system -> system channel
otherwise -> messages channel
```

এতে future call/message/system notification আলাদা behavior পাবে।

## Permission check

Android 13+ হলে notification দেখানোর আগে permission check হয়:

```text
POST_NOTIFICATIONS granted?
```

Permission না থাকলে app crash করে না; শুধু notification show করে না।

## Notification tap করলে কী হয়?

এখন notification tap করলে `MainActivity` open হয়।

Specific chat deep-link এখনো করা হয়নি। সেটা পরের notification navigation step।

## কীভাবে verify করবে?

1. App run করো।
2. Notification permission allow করো।
3. Firestore থেকে device token নাও।
4. Firebase/FCM test payload পাঠাও।
5. Notification আসে কিনা দেখো।
6. Tap করলে app open হয় কিনা দেখো।

## শেখার বিষয়

Notification feature তিন ভাগে হয়:

```text
1. token save
2. message receive/render
3. tap/deep-link navigation
```

আমরা এখন দ্বিতীয় অংশের foundation করলাম।
