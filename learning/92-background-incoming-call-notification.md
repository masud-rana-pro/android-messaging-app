# Step 92: Background Incoming Call Notification (Bangla Explanation)

এই ধাপে আমরা কল আসার নোটিফিকেশন সিস্টেমটি উন্নত করেছি যাতে অ্যাপ বন্ধ থাকলেও বা ব্যাকগ্রাউন্ডে থাকলেও ইউজার কল পেতে পারে। এর জন্য আমরা **Cloudflare Worker** ব্যবহার করেছি যা Firebase এবং Android এর মধ্যে একটি নিরাপদ সেতু (Bridge) হিসেবে কাজ করে।

## এটি কিভাবে কাজ করে?

১. **FCM Token Mirroring**: আপনার ফোন যখনই নতুন কোনো FCM টোকেন পায়, সেটি এখন থেকে `users/{uid}/fcmToken` ফিল্ডে সেভ হবে। এতে সার্ভার (Worker) সহজেই জানতে পারবে কোন ফোনে নোটিফিকেশন পাঠাতে হবে।
২. **Worker Trigger**: যখন কেউ কল দেয় (`OutgoingCallViewModel`), তখন অফার তৈরি হওয়ার পর আমাদের Cloudflare Worker URL-এ একটি রিকোয়েস্ট পাঠানো হয়।
৩. **Cloudflare Worker (The Bridge)**:
    *   এটি কলারের কাছ থেকে `callId` এবং `receiverId` নেয়।
    *   Firebase Service Account ব্যবহার করে গুগল থেকে একটি টোকেন সংগ্রহ করে।
    *   Firestore থেকে রিসিভারের FCM টোকেন খুঁজে বের করে।
    *   রিসিভারের ফোনে একটি হাই-প্রায়োরিটি "incoming_call" নোটিফিকেশন পাঠায়।
৪. **Android Notification**: রিসিভারের ফোনে নোটিফিকেশন আসলে তাতে ক্লিক করলে সরাসরি ইনকামিং কল স্ক্রিন খুলে যাবে।

## কেন Cloudflare Worker?
সরাসরি Android অ্যাপ থেকে FCM নোটিফিকেশন পাঠানো নিরাপদ নয় কারণ এতে Firebase-এর গোপন চাবি (Service Account Key) অ্যাপের ভেতরে রাখতে হয়, যা হ্যাকাররা সহজেই বের করে নিতে পারে। Cloudflare Worker-এ এই চাবিগুলো সুরক্ষিত থাকে এবং এটি কোনো ক্রেডিট কার্ড ছাড়াই ফ্রিতে ব্যবহার করা যায়।

## আপনার জন্য করণীয়:
আপনি যেহেতু অলরেডি সিক্রেটগুলো কনফিগার করেছেন, তাই এখন অ্যাপটি রান করলে ব্যাকগ্রাউন্ডেও কল নোটিফিকেশন পাওয়ার কথা।

পরবর্তী ধাপ: **Step 93: Microphone, speaker, and foreground service polish.**
