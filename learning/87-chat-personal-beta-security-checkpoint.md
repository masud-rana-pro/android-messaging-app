# ধাপ ৮৭: Chat personal-beta security checkpoint

## Read receipt spoofing gap

Conversation document-এ `readAtByUser` একটি map। আগের rule শুধু top-level changed field `readAtByUser` কিনা দেখত। ফলে participant modified client দিয়ে পুরো map পাঠিয়ে peer user-এর timestamp-ও বদলানোর চেষ্টা করতে পারত। Seen state বিশ্বাসযোগ্য রাখতে প্রত্যেক user শুধু নিজের receipt লিখতে পারা জরুরি।

## Nested map diff

নতুন `isOwnReadReceiptUpdate()` rule function পুরোনো receipt map নেয়। Map আগে না থাকলে empty map ধরা হয়। এরপর rule নিশ্চিত করে:

- conversation-এর changed top-level field শুধু `readAtByUser`;
- নতুন receipt value একটি map;
- nested map-এর changed key শুধু `request.auth.uid`;
- নিজের timestamp value অবশ্যই `request.time`।

তাই client arbitrary পুরোনো/ভবিষ্যৎ timestamp লিখতে, peer receipt বদলাতে বা অন্য key delete করতে পারে না। Android-এর `FieldValue.serverTimestamp()` write rules evaluation-এ `request.time` হিসেবে resolve হয়।

## Preview update আলাদা কেন

Message send/edit/delete conversation-এর preview fields update করে। এই field list থেকে `readAtByUser` সরানো হয়েছে এবং own-receipt update আলাদা branch পেয়েছে। এতে preview update-এর সঙ্গে receipt map গোপনে বদলানো যায় না।

## Chat code checkpoint

Personal-beta core code path-এ এখন text/image/document, reply/edit/delete, unread/seen, typing/presence, block/report, sync retry, notification fanout ও deep-link foundation আছে। Forward, star, reaction, status/channel personal-beta message/call লক্ষ্যটির অংশ নয়।

## যা device ছাড়া confirm করা যাবে না

Real completion pass-এর জন্য rules ও Cloud Function deploy করে দুইটি Android phone-এ পরীক্ষা করতে হবে:

1. A থেকে B-তে text/image/document পাঠানো।
2. B background থাকলে notification পাওয়া এবং tap করে chat খোলা।
3. B chat খুললে A-তে Seen হওয়া।
4. Reply/edit/delete দুই phone-এ live update হওয়া।
5. Block করলে send ও notification বন্ধ হওয়া।
6. Network off/on করে sync error ও retry যাচাই।

Firebase CLI বর্তমান environment-এ নেই এবং low-token নির্দেশনায় build/test চালানো হয়নি। তাই code checkpoint complete হলেও deployment/device pass সৎভাবে pending রাখা হয়েছে।
