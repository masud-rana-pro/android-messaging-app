# Step 60 - Notification Chat Deep Link

এই ধাপে notification tap করলে specific chat open করার foundation করেছি।

## আগে কী ছিল?

Notification tap করলে শুধু app open হতো। কিন্তু কোন chat-এর notification এসেছে, সেই chat open হচ্ছিল না।

## এখন কী হবে?

FCM payload-এ যদি `conversationId` থাকে, notification tap করলে app সেই chat খুলতে পারবে।

## Payload কেমন হবে?

```json
{
  "type": "message",
  "conversationId": "direct_uidA_uidB",
  "title": "Masud Rana",
  "photoUrl": "https://res.cloudinary.com/...",
  "body": "New message"
}
```

## 1. Notification extras

`ContactMeNotificationRenderer` এখন PendingIntent-এ extras দেয়:

```text
conversationId
chat title
chat photo URL
```

মানে notification tap করলে `MainActivity` এই data পাবে।

## 2. `NotificationNavigation`

নতুন helper:

```text
apps/ContactMe/app/src/main/java/com/contactme/app/navigation/NotificationNavigation.kt
```

এর কাজ:

```text
Intent extras -> ChatTarget
```

## 3. Cold start handling

App বন্ধ থাকলে notification tap করলে `MainActivity.onCreate()` চলে। সেখানে intent থেকে `ChatTarget` read করা হয়।

## 4. Warm start handling

App আগে থেকেই open থাকলে notification tap করলে `onNewIntent()` চলে। সেখানে নতুন intent থেকে chat target update হয়।

## 5. ContactMeApp navigation

`ContactMeApp` notification target পেলে authenticated app screen থেকে chat open করে।

Auth/Profile setup screen-এ force করে chat open করে না, কারণ signed-out user আগে login/profile complete করবে।

## কীভাবে verify করবে?

1. App install/run করো।
2. Notification permission allow করো।
3. FCM payload পাঠাও যেখানে `conversationId` আছে।
4. Notification tap করো।
5. Matching chat open হওয়া উচিত।

## শেখার বিষয়

Notification deep-link করতে শুধু notification দেখানো যথেষ্ট না। Payload থেকে route data নিতে হয়, Activity intent handle করতে হয়, তারপর app navigation state update করতে হয়।
