# ধাপ ৭৬: Group message-এর sender name

## Group-এ নাম কেন দরকার

Direct chat-এ incoming message কার তা header থেকেই বোঝা যায়। Group-এ একাধিক member থাকায় প্রতিটি incoming bubble-এর উপরে sender name না থাকলে কথোপকথন বোঝা যায় না।

## Message document-এ নাম রাখা হয়নি কেন

Client যদি `senderDisplayName` field নিজে লিখতে পারে, modified app অন্য কারও নাম লিখে spoof করতে পারে। তাই repository message-এর trusted `senderId` নিয়ে `users/{senderId}` profile পড়ে local model-এ display name যোগ করে। Database message content-এ forged label বিশ্বাস করা হয় না।

## Cache

একই sender-এর প্রতিটি message-এর জন্য profile read করা ব্যয়বহুল। `ConcurrentHashMap` sender ID অনুযায়ী resolved name cache করে। নতুন snapshot-এ শুধু cache-এ না থাকা sender profile পড়া হয়। এটি thread-safe, কারণ Firestore callback coroutine থেকে access হতে পারে।

## UI rule

Name label শুধু তখন দেখায় যখন:

- conversation type group;
- message current user-এর নয়।

Direct chat ও নিজের bubble আগের মতো compact থাকে। Profile name পাওয়া না গেলে app-standard fallback **Group member** দেখায়।

## যাচাই

তিন account-এর group-এ আলাদা account থেকে text ও photo পাঠান। প্রতিটি incoming bubble-এর name Firebase profile-এর display name-এর সঙ্গে মিলছে কি না দেখুন।
