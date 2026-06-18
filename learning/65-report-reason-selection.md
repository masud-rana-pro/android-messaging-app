# ধাপ ৬৫: রিপোর্ট করার কারণ নির্বাচন

## এই ধাপ কেন দরকার

আগে chat থেকে **Report** চাপলে অ্যাপ সব রিপোর্টকে `other` কারণ হিসেবে Firestore-এ পাঠাত। এতে moderation করার সময় বোঝা যেত না সমস্যাটি spam, harassment নাকি scam। এখন ব্যবহারকারী রিপোর্ট পাঠানোর আগে সঠিক কারণ নির্বাচন করতে পারবেন।

## কোডের flow

### ১. UI dialog

`ChatActionMenu`-এ Report চাপলে `isReportDialogVisible` state `true` হয়। Compose এই state দেখে `ReportReasonDialog` দেখায়। Dialog বন্ধ বা reason নির্বাচন হলে state আবার `false` হয়।

### ২. enum থেকে option

Dialog সরাসরি `ReportReason.entries` ব্যবহার করে। ফলে enum-এ নতুন reason যোগ করলে UI-তেও সেটি পাওয়া যাবে। `displayLabel()` database value-কে ব্যবহারকারীর জন্য পরিষ্কার text-এ দেখায়, যেমন `Scam`-কে **Scam or fraud**।

### ৩. ViewModel-এ selected reason

`reportCurrentChat(reason: ReportReason)` এখন UI থেকে নির্বাচিত enum নেয়। ViewModel এই reason repository-তে পাঠায়। তাই UI Firestore নিয়ে জানে না এবং repository UI state নিয়ে জানে না; প্রতিটি layer-এর দায়িত্ব আলাদা থাকে।

### ৪. Firestore value

`ReportReason` enum-এর `firestoreValue` হলো স্থায়ী lowercase value:

- `Spam` → `spam`
- `Harassment` → `harassment`
- `Scam` → `scam`
- `Other` → `other`

UI label ভবিষ্যতে বদলালেও database query ভাঙবে না, কারণ সংরক্ষিত value আলাদা ও স্থিতিশীল।

## কীভাবে যাচাই করবেন

1. দুইটি account দিয়ে একটি real conversation খুলুন।
2. উপরের তিন-ডট menu থেকে **Report** চাপুন।
3. একটি reason নির্বাচন করুন।
4. screen-এ **Report sent.** message আসছে কি না দেখুন।
5. Firebase Console-এর report collection খুলে `reason` field-এ নির্বাচিত lowercase value আছে কি না যাচাই করুন।

## এই ধাপে যা শেখা হলো

- Compose state দিয়ে dialog দেখানো ও লুকানো
- enum থেকে নির্ভরযোগ্য option list তৈরি
- callback-এর মাধ্যমে UI থেকে ViewModel-এ typed data পাঠানো
- display text এবং database value আলাদা রাখা
