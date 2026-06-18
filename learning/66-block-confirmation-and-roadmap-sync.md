# ধাপ ৬৬: Block confirmation ও roadmap sync

## কেন confirmation দরকার

Block একটি গুরুত্বপূর্ণ এবং তাৎক্ষণিক action। Menu-তে ভুল touch হলে সরাসরি block হয়ে যাওয়া ভালো user experience নয়। এখন **Block** চাপার পর confirmation dialog আসে; ব্যবহারকারী নিশ্চিত করলে তবেই ViewModel ও repository call হয়।

## State কীভাবে কাজ করে

`isBlockDialogVisible` একটি Compose state। এর মান `true` হলে dialog compose হয় এবং `false` হলে UI থেকে সরে যায়।

- Block menu item → state `true`
- Cancel বা outside dismiss → state `false`
- Confirm → আগে state `false`, তারপর `onBlockChat()`

আগে dialog বন্ধ করায় network operation চলার সময় পুরোনো dialog screen-এ আটকে থাকে না। ViewModel-এর `isSafetyActionInProgress` আগের মতো operation-এর loading ও duplicate action নিয়ন্ত্রণ করে।

## Unblock কেন confirmation ছাড়া রাখা হয়েছে

Unblock restrictive action ফিরিয়ে নেয় এবং messaging পুনরায় চালু করে। তাই এই ধাপে শুধু destructive **Block** action confirmation পেয়েছে। ভবিষ্যতে product requirement বদলালে একই pattern দিয়ে Unblock-এও dialog দেওয়া যাবে।

## Roadmap sync কেন করা হয়েছে

Code-এ block/report data model, Firestore rules, message enforcement এবং UI তৈরি হলেও roadmap-এ এখনও `Not started` ছিল। Planning document ভুল থাকলে পরের কাজ ভুল priority-তে যেতে পারে। তাই status এখন `Done foundation`; emulator tests ও admin moderation-কে পরবর্তী কাজ হিসেবে রাখা হয়েছে।

## যাচাই

1. Chat-এর তিন-ডট menu খুলে **Block** চাপুন।
2. **Cancel** করলে message composer চালু আছে কি না দেখুন।
3. আবার Block খুলে **Block** confirm করুন।
4. composer বন্ধ এবং status message দৃশ্যমান কি না দেখুন।
5. Firestore-এর `blocked_users/{currentUserId}/items/{peerUserId}` document যাচাই করুন।
