# ধাপ ৭৭: Polished chat list ও filter

## কেন Home screen redesign করা হলো

আগের screen-এ conversation, saved contacts এবং user search একসঙ্গে সবসময় দেখানো হতো। এতে chat list scan করা কঠিন ছিল এবং প্রতিটি row আলাদা card হওয়ায় messaging app-এর বদলে dashboard-এর মতো লাগত। এখন default screen শুধু conversation-কেন্দ্রিক; people results search করার সময় আসে।

## এক search field-এর flow

Search query একই সঙ্গে দুই জায়গায় ব্যবহৃত হয়:

- existing conversation-এর title ও last-message text localভাবে filter করে;
- ContactDiscoveryViewModel username/phone অনুযায়ী remote user খোঁজে।

Saved contact আগে দেখানো হয় এবং একই user remote result-এ থাকলে duplicate বাদ দেওয়া হয়। Query খালি হলে people section পুরোপুরি লুকানো থাকে।

## Functional filter

- **All**: সব conversation
- **Unread**: `hasUnreadMessages == true`
- **Groups**: `ConversationType.Group`

Favorites এখনো database model-এ নেই, তাই শুধু design নকল করার জন্য অকার্যকর Favorites chip রাখা হয়নি। Favorite/pin model তৈরি হলে সেটি বাস্তব filter হিসেবে যোগ হবে।

## Fake unread count কেন সরানো হয়েছে

বর্তমান schema শুধু unread আছে কি না জানে; exact unread message count জানে না। তাই hardcoded `1` দেখানো ভুল ছিল। এখন ছোট primary-color dot সত্যভাবে শুধু unread state বোঝায়। পরের unread-count schema step-এ real সংখ্যা যোগ করা যাবে।

## Flat conversation row

Chat list repeated content, তাই প্রতিটি item-কে floating card না বানিয়ে avatar, title, preview, timestamp ও unread dot দিয়ে compact row করা হয়েছে। ৮dp interaction shape touch feedback দেয় কিন্তু list-কে card grid বানায় না।

## Navigation icons

Text-এর প্রথম অক্ষর দিয়ে icon দেখানোর বদলে Material icons ব্যবহার হয়েছে। আপাতত শুধু Chats ও Calls visible, কারণ অন্য tabs এখনো বাস্তবে implement হয়নি। এতে user অকার্যকর Coming soon screen-এ যান না।

## যাচাই

1. All filter-এ direct ও group chat দেখুন।
2. Unread filter-এ শুধু unread chat দেখুন।
3. Groups filter-এ শুধু group দেখুন।
4. নাম/last message দিয়ে existing chat search করুন।
5. username বা phone দিয়ে নতুন user search করুন।
6. light ও dark theme-এ row contrast, timestamp ও unread dot পরীক্ষা করুন।
