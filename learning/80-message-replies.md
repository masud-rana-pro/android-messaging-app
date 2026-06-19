# ধাপ ৮০: Message reply

## User flow

যেকোনো message bubble long-press করলে composer-এর উপরে reply preview আসে। Preview-তে sender name ও original content-এর সংক্ষিপ্ত রূপ থাকে। Cancel icon reply mode বন্ধ করে এবং Send করলে নতুন text message-এর সঙ্গে reply context সংরক্ষিত হয়।

## `MessageReply` model

Reply-এর জন্য পুরো original message duplicate করা হয়নি। শুধু প্রয়োজনীয় data রাখা হয়েছে:

- original message ID
- sender display name
- compact preview
- original message type

এতে reply document ছোট থাকে এবং original image/document URL অপ্রয়োজনীয়ভাবে copy হয় না।

## Type-aware preview

- Text → প্রথম ১২০ character
- Image → `Photo`
- Document → original filename

এই preview composer ও sent bubble—দুই জায়গায় একই model থেকে render হয়।

## ViewModel state

`replyingTo` nullable state। `startReply()` এটি set করে, `cancelReply()` null করে এবং successful send-এর পরে automatically clear হয়। Send fail হলে reply state থাকে, ফলে user context হারানো ছাড়াই retry করতে পারেন।

## Firestore fields

- `replyToMessageId`
- `replyToSenderName`
- `replyPreview`
- `replyType`

পুরোনো message-এ fieldগুলো না থাকলে `replyTo` null হয়, তাই backward compatibility থাকে।

## Long-press detection

Bubble modifier `pointerInput` ও `detectTapGestures(onLongPress)` ব্যবহার করে। সাধারণ scrolling behavior বজায় রেখে long press reply action চালায়।

## Manual verification

1. Incoming text message long-press করে reply পাঠান।
2. Photo ও document message-এ reply করুন।
3. Cancel icon পরীক্ষা করুন।
4. Network failure-এর পরে reply context থাকে কি না দেখুন।
5. Sender ও receiver—দুই account-এ quoted preview মিলিয়ে দেখুন।

## Build note

Codex usage limit বাঁচাতে এই ধাপে Gradle build/test ইচ্ছাকৃতভাবে চালানো হয়নি। পরবর্তী combined verification checkpoint-এ একবারে build চালানো হবে।
