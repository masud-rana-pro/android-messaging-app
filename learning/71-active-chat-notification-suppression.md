# ধাপ ৭১: খোলা chat-এর notification বন্ধ রাখা

## সমস্যাটি কী ছিল

User যে conversation screen-এ message পড়ছেন, সেই conversation-এ নতুন message এলে app আবার system notification দেখাত। এতে একই তথ্য screen ও notification tray দুই জায়গায় এসে বিরক্তিকর experience তৈরি করে।

## দুটি state একসঙ্গে কেন দরকার

শুধু active conversation ID জানলেই যথেষ্ট নয়। App background-এ গেলেও Compose screen memory-তে থাকতে পারে। তাই suppression-এর জন্য দুইটি condition প্রয়োজন:

1. app process foreground-এ আছে;
2. incoming `conversationId` বর্তমানে খোলা conversation-এর সমান।

যেকোনো একটি false হলে notification দেখানো হয়।

## `ProcessLifecycleOwner`

এটি পুরো application process-এর lifecycle দেয়। কোনো Activity দৃশ্যমান হলে `onStart`, আর app background-এ গেলে `onStop` পাওয়া যায়। `ContactMeApplication` এই event tracker-এ পাঠায়।

## `DisposableEffect`

Chat screen compose হলে active conversation set হয়। Screen composition থেকে বের হলে `onDispose` শুধু একই conversation ID clear করে। `compareAndSet` ব্যবহারে পুরোনো screen নতুন screen-এর active ID ভুল করে clear করতে পারে না।

## Thread safety

FCM service ও Compose UI আলাদা thread থেকে tracker ব্যবহার করতে পারে। তাই `AtomicBoolean` ও `AtomicReference` ব্যবহার করা হয়েছে; read/write race condition ছাড়াই হয়।

## Test

- foreground + একই chat → suppress
- background + একই chat → notification
- foreground + অন্য chat → notification

এই policy test emulator ছাড়াই JVM-এ চলে।
