# ধাপ ৬৮: Message notification fanout

## কেন backend function দরকার

Android app-এর মধ্যে FCM server credential রাখা নিরাপদ নয়। Message পাঠানো user যদি সরাসরি অন্য user-এর token-এ notification পাঠায়, credential চুরি ও abuse হওয়ার ঝুঁকি থাকে। তাই Firestore-এ message তৈরি হলে trusted Firebase Cloud Function notification পাঠায়।

## সম্পূর্ণ flow

1. Android app `conversations/{conversationId}/messages/{messageId}` document তৈরি করে।
2. `sendMessageNotification` trigger নতুন document শনাক্ত করে।
3. Conversation থেকে participant IDs পড়ে sender বাদ দেওয়া হয়।
4. দুই পক্ষের block document পরীক্ষা করা হয়। Block থাকলে notification পাঠানো হয় না।
5. Recipient-এর সব registered device token পড়া হয়।
6. FCM সর্বোচ্চ 500 token-এর batch-এ notification পাঠায়।
7. Expired/invalid token Firestore থেকে মুছে ফেলা হয়।

## Payload-এর fields

- `type`: Android renderer কোন channel ব্যবহার করবে
- `conversationId`: notification tap করলে সঠিক chat খুলবে
- `messageId`: ভবিষ্যৎ deduplication/action-এর পরিচয়
- `title`: sender display name
- `body`: text preview বা `Photo`
- `photoUrl`: chat header-এর sender photo

Text preview 120 character-এ সীমাবদ্ধ, ফলে notification অস্বাভাবিক বড় হয় না। সম্পূর্ণ message document payload-এ পাঠানো হয় না।

## Invalid token cleanup

App uninstall বা FCM token বাতিল হলে send response-এ `invalid-registration-token` বা `registration-token-not-registered` আসে। Function সংশ্লিষ্ট device document delete করে, তাই একই dead token-এ বারবার request যায় না।

## Local verification

```powershell
cd backend/firebase-functions
npm install
npm run build
```

TypeScript build সফল হলে type mismatch ও syntax error নেই। Real notification যাচাই করতে function deploy, দুইটি account এবং দুইটি device/emulator প্রয়োজন।

## Manual prerequisite কখন লাগবে

Cloud Functions deploy করতে Firebase CLI login ও supported billing plan প্রয়োজন। Local implementation/build-এর জন্য Firebase Console পরিবর্তন দরকার নেই।
