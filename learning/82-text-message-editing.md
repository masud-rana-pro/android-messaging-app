# ধাপ ৮২: নিজের text message edit করা

## UI flow

নিজের live text message long-press করলে এখন **Edit** action দেখা যায়। Image, document, অন্য user-এর message এবং deleted message edit করা যায় না। Edit চাপলে পুরোনো text composer-এ আসে; cancel icon চাপলে edit mode ও draft দুটোই পরিষ্কার হয়।

## একই composer কেন

আলাদা edit dialog না বানিয়ে existing composer ব্যবহার করায় text limit, send button এবং error state একই থাকে। `editingMessageId` null হলে send নতুন message তৈরি করে; ID থাকলে একই message document update করে। Reply এবং edit একই সময়ে active রাখা হয়নি, তাই mode ambiguity থাকে না।

## Repository transaction

`editMessage(...)` প্রথমে trimmed text validate করে। Transaction-এর ভিতরে message ও conversation দুটো document write-এর আগে read করা হয়। এরপর নিশ্চিত করা হয়:

- sender বর্তমান user;
- message type `text`;
- message deleted নয়;
- text খালি নয় এবং সর্বোচ্চ ৪০০০ character।

সফল edit-এ শুধু `text` বদলায় এবং `editedAt = serverTimestamp` লেখা হয়। Message ID ও `createdAt` অপরিবর্তিত থাকে, তাই chat chronology নড়ে না।

## Latest preview

Conversation-এর `lastMessageId` edited message-এর ID হলে `lastMessageText`-ও নতুন text পায়। পুরোনো message edit করলে chat-list-এর current preview বদলায় না।

## Firestore rules

Update rule-এর edit branch sender ownership enforce করে এবং changed fields শুধু `text`, `editedAt`-এ সীমাবদ্ধ রাখে। ফলে modified client sender/type/createdAt বা media metadata বদলাতে পারে না। Existing soft-delete branch আলাদাই থাকে।

## Edited marker

Snapshot mapper `editedAt` timestamp-কে `editedAtMillis` করে। মান শূন্যের বেশি হলে message time-এর পাশে `edited` label দেখা যায়।

## Manual verification

1. নিজের text message long-press করে Edit চাপুন।
2. text বদলে send করুন এবং bubble-এ `edited` দেখুন।
3. latest message edit করলে chat-list preview বদলেছে কিনা দেখুন।
4. image, document ও অন্য user-এর message-এ Edit নেই নিশ্চিত করুন।
5. edit cancel করলে composer পরিষ্কার হয় কিনা দেখুন।
6. rules deploy করার পরে modified client দিয়ে unauthorized field update reject হচ্ছে কিনা যাচাই করুন।

## Build note

Low-token নির্দেশনা অনুযায়ী Gradle build/test চালানো হয়নি। এই ধাপে targeted source review এবং `git diff --check` করা হয়েছে।
