# ধাপ ৮৪: Background message notification delivery

## Mixed payload-এর সমস্যা

FCM payload-এ `notification` এবং `data` দুটো থাকলে foreground-এ `FirebaseMessagingService` message পায়, কিন্তু background-এ Android নিজে notification render করতে পারে। তখন ContactMe renderer-এর বানানো custom pending-intent extras থাকে না। ফলাফল: notification দেখা গেলেও tap করে নির্দিষ্ট conversation খোলার flow অসামঞ্জস্যপূর্ণ হতে পারে।

## Data-only সমাধান

Cloud Function এখন শুধু `data` payload পাঠায় এবং Android priority `high` রাখে। ফলে foreground ও background—দুই অবস্থায় app-এর `ContactMeMessagingService` একই payload গ্রহণ করে। এরপর `ContactMeNotificationRenderer`:

- notification channel নির্বাচন করে;
- active conversation হলে duplicate notification suppress করে;
- conversation id/type/title/photo custom extras-এ রাখে;
- tap করলে সঠিক chat target খুলতে পারে।

## TTL

Android message TTL ২৪ ঘণ্টা রাখা হয়েছে। Device সাময়িক offline থাকলে FCM এই সময়ের মধ্যে delivery চেষ্টা করতে পারে; একদিন পরে পুরোনো chat notification আর পাঠানোর প্রয়োজন নেই।

## Payload contract

Backend এখনো পাঠায়:

- `type = message`
- `conversationId`
- `conversationType`
- `messageId`
- `title`
- `body`
- `photoUrl`

সব value string, তাই Android `RemoteMessage.data` contract অপরিবর্তিত থাকে।

## Deployment blocker

বর্তমান machine-এ Firebase CLI install নেই। Real delivery চালু করতে CLI login, `messasing-app-9c367` project access এবং Cloud Functions সমর্থিত billing plan প্রয়োজন। Function deploy না হওয়া পর্যন্ত code repository-তে ready থাকলেও production trigger update হবে না।

## Manual verification

1. Function deploy করুন।
2. Recipient app foreground-এ রেখে message পাঠিয়ে notification behavior দেখুন।
3. Recipient app background-এ রেখে আবার message পাঠান।
4. Notification tap করে ঠিক conversation খুলছে নিশ্চিত করুন।
5. একই conversation খোলা অবস্থায় foreground notification suppress হচ্ছে দেখুন।

## Build note

Low-token নির্দেশনা অনুযায়ী function build/test চালানো হয়নি। Targeted payload-contract review ও `git diff --check` করা হয়েছে।
