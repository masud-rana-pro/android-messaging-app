# ধাপ ৮৬: Message sync ও send reliability

## Listener error আগে কী করত

Firestore snapshot listener error হলে repository আগে empty list emit করত। UI তখন network/rules failure-কে “No messages yet” হিসেবে দেখাত। এতে user বুঝতে পারত না chat সত্যিই খালি, নাকি sync ব্যর্থ হয়েছে।

## Error propagation

এখন listener error হলে `callbackFlow` error দিয়ে close হয়। ViewModel-এর `catch` block:

- loading বন্ধ করে;
- আলাদা `messageLoadError` set করে;
- আগে পাওয়া message থাকলে সেগুলো রেখে দেয়।

UI **Messages unavailable** state এবং Retry button দেখায়। Retry পুরোনো job cancel করে একই conversation-এর জন্য fresh listener শুরু করে। নতুন snapshot এলে error clear হয়।

## Duplicate-send race

আগে `isSending = true` coroutine-এর ভিতরে set হতো। User খুব দ্রুত send দুবার চাপলে দ্বিতীয় callback coroutine চালুর আগেই পুরোনো false state দেখতে পারত। ফলে একই action দুইবার শুরু হওয়ার ছোট race window ছিল।

এখন text/edit, delete, image ও document path coroutine launch-এর আগেই synchronously `isSending` true করে। Main-thread-এর পরের callback সঙ্গে সঙ্গে busy state দেখে return করে।

## Retry semantics

- Stream Retry শুধু Firestore listener পুনরায় subscribe করে।
- Failed text draft composer-এ থাকে, তাই send Retry একই draft ব্যবহার করে।
- Image/document-এর existing WorkManager retry path অপরিবর্তিত থাকে।

## Manual verification

1. Firestore/network unavailable করে chat খুলুন; empty state-এর বদলে sync error দেখুন।
2. Network ফিরিয়ে Retry চাপুন এবং messages load হচ্ছে দেখুন।
3. Send button দ্রুত একাধিকবার চাপুন; একটি message তৈরি হচ্ছে নিশ্চিত করুন।
4. Image/document picker result দ্রুত repeat হলেও একটি queue operation শুরু হচ্ছে দেখুন।
5. আগে load হওয়া message থাকা অবস্থায় listener fail করলে পুরোনো messages হারায় না নিশ্চিত করুন।

## Build note

Low-token নির্দেশনা অনুযায়ী Gradle build/test চালানো হয়নি। Targeted source-flow review ও `git diff --check` করা হয়েছে।
