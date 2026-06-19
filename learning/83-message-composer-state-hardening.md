# ধাপ ৮৩: Message composer state hardening

## সমস্যাটি কী ছিল

Reply, edit ও attachment একই composer-এর আশেপাশে কাজ করে। তাই edit mode active থাকা অবস্থায় Reply বা attachment শুরু করলে একাধিক mode-এর state মিশে যেতে পারত। Upload চলার সময় long-press action খুললেও একই ধরনের race তৈরি হতো।

## Mutually-exclusive mode

এখন Reply শুরু করলে:

- পুরোনো draft clear হয়;
- `editingMessageId` null হয়;
- reply metadata set হয়;
- stale typing state false হয়।

Edit শুরু করলে reply mode clear হয় এবং message-এর বর্তমান text composer-এ আসে। Upload/send চললে Reply বা Edit শুরু হয় না।

## Attachment guard

Edit mode-এ attachment icon disabled থাকে। শুধু UI guard-এর ওপর নির্ভর করা হয়নি; `sendImageMessage(...)` ও `sendDocumentMessage(...)` ViewModel entry point-ও edit active থাকলে return করে। ফলে অন্য UI path থেকে method call হলেও conflicting send হয় না।

## Delete cleanup

যে message edit করা হচ্ছে সেটি delete হলে `editingMessageId` এবং সংশ্লিষ্ট composer text দুটোই clear হয়। অন্য message delete করলে বর্তমান draft অপরিবর্তিত থাকে।

## Manual verification

1. Edit mode খুলে attachment icon disabled দেখুন।
2. Edit থেকে অন্য message Reply করলে edit draft clear হয়েছে দেখুন।
3. Upload চলার সময় message long-press action না খোলা নিশ্চিত করুন।
4. Edit করা message delete করলে composer clear হয়েছে দেখুন।
5. সাধারণ reply, edit এবং media send আলাদাভাবে আগের মতো কাজ করছে যাচাই করুন।

## Build note

Low-token নির্দেশনা অনুযায়ী Gradle build/test চালানো হয়নি। Targeted source review ও `git diff --check` করা হয়েছে।
