# ধাপ ৭৫: Group ও direct chat behavior আলাদা করা

## ভুল behavior কী হতে পারত

Group document-এ একাধিক participant থাকে। Direct chat logic প্রথম অন্য user-কে peer ধরে presence, typing, read receipt ও block state পড়ত। Group খুললে এতে random member-এর last seen দেখা এবং তাকে block/report করার মতো ভুল behavior হতো।

## Conversation type navigation

Conversation list-এর `ConversationPreview.type` এখন `ChatTarget` হয়ে `ChatDetailScreen` ও ViewModel পর্যন্ত যায়। ফলে screen শুধু title দেখে অনুমান করে না; typed model অনুযায়ী behavior নির্বাচন করে।

## Group-এর জন্য বন্ধ direct features

- peer presence listener
- one-to-one typing listener/write
- peer read receipt
- block/report/unblock menu
- conversation-peer safety lookup

Header-এ last seen-এর বদলে **Group** দেখায়। Group typing ও member-specific read receipt পরে group-aware model দিয়ে আলাদাভাবে যোগ হবে।

## Message safety

Direct message পাঠানোর আগে দুই user-এর block relation পরীক্ষা হয়। Group-এ প্রথম participant-কে peer ধরে এই check করা ভুল। Repository conversation type `group` হলে direct block lookup skip করে; Firestore membership rule group access নিয়ন্ত্রণ করে।

## যাচাই

1. Group list item খুলুন।
2. Header-এ Group এবং কোনো block/report menu নেই নিশ্চিত করুন।
3. Text ও image message পাঠান।
4. অন্য member account-এ message পৌঁছায় কি না দেখুন।
