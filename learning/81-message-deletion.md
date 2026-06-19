# ধাপ ৮১: নিজের message delete করা

## Long-press action

Message long-press করলে **Reply** ও প্রয়োজন অনুযায়ী **Delete** action আসে। অন্য user-এর message-এ Delete দেখায় না। Deleted message আবার long-press করা যায় না।

## Soft delete কেন

Message document সম্পূর্ণ delete করলে chronology, reply reference ও moderation evidence হঠাৎ হারিয়ে যায়। তাই document রেখে content fields clear করা হয় এবং:

- `isDeleted = true`
- `deletedAt = serverTimestamp`

রাখা হয়। Listener একই message ID পায়, কিন্তু UI content-এর বদলে **This message was deleted** দেখায়।

## Transaction ownership

Repository transaction message document পড়ে নিশ্চিত করে `senderId == currentUserId`। এরপর content clear করে। Firestore rules একই ownership server-side enforce করে; modified client অন্য user-এর message delete করতে পারে না।

## যেসব content clear হয়

- text
- media URL/public ID/provider
- MIME type
- filename/file size

Reply metadata bubble-এ render হয় না, কারণ `isDeleted` branch অন্য সব content লুকায়।

## Chat-list preview

নতুন message send-এর সময় conversation document-এ `lastMessageId` রাখা হয়। Delete transaction message ID latest ID-এর সমান হলে `lastMessageText` → **Message deleted** করে। পুরোনো message delete করলে current preview বদলায় না।

## Cloudinary limitation

Current unsigned upload architecture-এ Android client-এর কাছে Cloudinary API secret নেই, তাই remote asset destroy করা নিরাপদভাবে সম্ভব নয়। Firestore URL সরানোর ফলে app থেকে file আর accessible নয়, কিন্তু production physical deletion signed backend function phase-এ যোগ করতে হবে।

## Manual verification

1. নিজের text/image/document long-press করুন।
2. Delete চাপুন এবং placeholder দেখুন।
3. অন্য user-এর message-এ Delete নেই নিশ্চিত করুন।
4. Latest message delete করলে chat-list preview দেখুন।
5. অন্য account থেকে unauthorized update reject হচ্ছে নিশ্চিত করুন।

## Build note

Usage limit বাঁচাতে Gradle build/test এই ধাপে চালানো হয়নি। Combined verification checkpoint-এ একবারে চালাতে হবে।
