# ধাপ ৭০: Notification payload hardening

## কেন payload validate করতে হয়

FCM payload অসম্পূর্ণ হলে আগের renderer blank `conversationId` নিয়েও notification দেখাতে পারত। User notification চাপলে সঠিক chat না খুলে invalid screen তৈরি হতে পারত। এখন message type-এর জন্য conversation ID বাধ্যতামূলক; না থাকলে notification render হয় না।

## Parser আলাদা করার কারণ

আগে parsing code renderer-এর private class-এর মধ্যে ছিল। এখন `ContactMeNotificationPayload.fromData()` pure map input নেয়। ফলে Firebase `RemoteMessage` বা Android emulator ছাড়াই JVM unit test দিয়ে parsing যাচাই করা যায়। Renderer শুধু validated model ব্যবহার করে notification বানায়।

## Android metadata

- `VISIBILITY_PRIVATE`: lock screen-এ Android privacy policy প্রয়োগ করতে পারে।
- `CATEGORY_MESSAGE`: system বুঝতে পারে এটি messaging notification।
- `groupKey`: একই conversation-এর notification একই logical group পায়।
- stable ID: একই conversation-এর নতুন notification আগেরটিকে update করে; ID সবসময় non-negative।

## Test cases

1. Conversation ID ছাড়া message payload reject হয়।
2. Valid message-এর title, body, photo, conversation ও group key ঠিক থাকে।
3. System notification conversation ছাড়াও fallback title/body দিয়ে render হতে পারে।

## যাচাই

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```

Test failure মানে parser contract ভেঙেছে; APK build failure মানে renderer integration-এ সমস্যা আছে।
