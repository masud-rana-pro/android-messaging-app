# ধাপ ৭৮: Chat detail UI polish

## Glyph-এর বদলে icon

আগে back, menu, attachment ও send action `<`, তিন-ডট text, `+`, `>` দিয়ে দেখানো হতো। এগুলো device/font অনুযায়ী ভিন্ন বা ভাঙা দেখাতে পারে। এখন Material icon ব্যবহারে familiar shape, consistent size এবং accessibility content description পাওয়া যায়।

## Date separator

একাধিক দিনের message একটানা list-এ থাকলে সময় বোঝা কঠিন। `itemsIndexed` বর্তমান message-এর আগের message দেখে calendar day বদলেছে কি না নির্ধারণ করে। Day বদলালে bubble-এর আগে:

- Today
- Yesterday
- অথবা `MMM d, yyyy`

দেখানো হয়। Timestamp না থাকলে safe fallback Today ব্যবহৃত হয়।

## Multiline composer

Message field এখন ১ থেকে ৪ line পর্যন্ত বাড়তে পারে। এতে দীর্ঘ message লিখলেও button বা screen layout অস্বাভাবিকভাবে সরে যায় না। Attachment icon field-এর trailing action এবং Send একটি stable circular button।

## Send validation

Text trim করার আগেই blank text পাঠানো ঠেকাতে Send button শুধু non-blank text থাকলে enabled হয়। Failed photo retry আলাদা error-row action দিয়েই হয়, তাই send button ভুল করে blank text retry করে না।

## Call icon এখন রাখা হয়নি কেন

UI দেখতে WhatsApp-এর মতো করার জন্য non-functional call/video icon রাখা industry-standard নয়। Call engine, state ও navigation তৈরি হলে real action হিসেবে icon যোগ হবে। এতে user dead control পান না।

## যাচাই

1. কয়েক দিনের message data দিয়ে date separator দেখুন।
2. চার line message লিখে composer height দেখুন।
3. blank text-এ Send disabled নিশ্চিত করুন।
4. attachment দিয়ে image পাঠান।
5. dark/light theme-এ icon ও bubble contrast পরীক্ষা করুন।
