# ধাপ ৭২: WorkManager দিয়ে background image message

## আগের flow-এর সমস্যা

আগে ViewModel coroutine সরাসরি Cloudinary upload করত। Screen বন্ধ, process pressure বা সাময়িক network failure হলে কাজটি থেমে যেতে পারত। এখন Android WorkManager কাজটি durable queue হিসেবে ধরে রাখে।

## কেন picker URI সরাসরি worker-এ দেওয়া হয়নি

Photo picker-এর URI permission process lifetime-এর সঙ্গে হারাতে পারে। তাই `PendingMediaStore` ছবিটি `filesDir/pending_media`-তে copy করে। এটি app-private এবং worker পরে একই file পড়তে পারে। সফল upload হলে file delete হয়।

## Queue flow

1. User photo নির্বাচন করেন।
2. Photo private pending file-এ copy হয়।
3. Network constraint সহ `ImageMessageWorker` enqueue হয়।
4. Network না থাকলে WorkManager অপেক্ষা করে।
5. Worker existing `MessageRepository.sendImageMessage()` চালায়।
6. Repository Cloudinary upload এবং Firestore message write করে।
7. Success হলে pending file delete হয়; repeated failure হলে UI retry option পায়।

## Hilt worker injection

Worker নিজে repository তৈরি করে না। `@HiltWorker` ও `HiltWorkerFactory` existing `MessageRepository` inject করে, ফলে একই safety check, Cloudinary client ও Firestore logic reuse হয়। Custom factory ব্যবহারের জন্য default WorkManager initializer manifest থেকে সরানো হয়েছে।

## Retry policy

Worker সর্বোচ্চ তিনবার চেষ্টা করে এবং exponential backoff ব্যবহার করে। অর্থাৎ server/network সাময়িক ব্যর্থ হলে সঙ্গে সঙ্গে request spam না করে ব্যবধান বাড়িয়ে retry হয়।

## UI state

ViewModel `WorkInfo` observe করে:

- queued/running → local image preview ও progress
- success → preview clear, message Firestore listener থেকে আসে
- final failure → local preview থাকে এবং Retry action পাওয়া যায়

## Manual verification

1. Chat খুলে network বন্ধ করুন।
2. Photo নির্বাচন করুন; preview/progress থাকা উচিত।
3. App background করুন এবং network চালু করুন।
4. WorkManager upload শেষ করলে message অন্য account-এ দেখা উচিত।
