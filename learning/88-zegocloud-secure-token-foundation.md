# ধাপ ৮৮: ZEGOCLOUD configuration ও secure token backend

## Secret APK-তে রাখা যাবে না কেন

APK reverse-engineer করা যায়। তাই ZEGOCLOUD ServerSecret বা AppSign Android resources, Gradle property বা Kotlin code-এ রাখলে attacker নিজের token বানাতে পারবে। এই ধাপে secret শুধু Firebase Secret Manager-এ থাকবে। Android authenticated callable endpoint থেকে short-lived token নেবে।

## Token request flow

1. Firebase Auth user Android থেকে শুধু `callId` পাঠায়।
2. Cloud Function `calls/{callId}` document পড়ে।
3. বর্তমান UID `participantIds`-এ আছে কিনা যাচাই করে।
4. ঠিক দুইজন distinct participant এবং call state `ringing`/`accepted` কিনা দেখে।
5. Call session expire হয়েছে কিনা server timestamp দিয়ে যাচাই করে।
6. Android-এর দেওয়া room গ্রহণ না করে document-এর trusted `roomId` নেয়।
7. ServerSecret দিয়ে ১০ মিনিটের Token04 তৈরি করে।
8. Android AppID, token, roomId এবং expiry পায়; secret পায় না।

## Strict room payload

Token payload-এ `room_id` এবং privilege map থাকে। Login-room ও publish-stream permission শুধু call document-এর room-এর জন্য দেওয়া হয়। General token না দেওয়ায় token চুরি হলেও অন্য arbitrary room-এ ব্যবহার করা কঠিন হয়।

## Token04 encoding

Official ZEGOCLOUD server-assistant format অনুসরণ করে token-এ expiry, random nonce, creation time, encrypted payload এবং AES-256-CBC ciphertext থাকে। IV cryptographically random করা হয়েছে এবং sensitive plaintext log করা হয় না। Token `04` prefix দিয়ে শুরু হয়।

## Android repository

`FirebaseCallTokenRepository` callable function invoke করে এবং response validate করে। Function region `asia-south1`, backend deployment region-এর সঙ্গে একই। Step 90-এর RTC engine এই repository থেকে credential নেবে।

## Step 89 dependency

এখনো `calls/{callId}` session তৈরি হয় না। এটি security-by-design: call participant যাচাই না করে token দেওয়া হবে না। Step 89 call state/document/rules যোগ করলে endpoint usable হবে।

## তোমার manual কাজ

1. [ZEGOCLOUD Console](https://console.zegocloud.com/) account খুলে RTC/Video Call project তৈরি করো।
2. Project-এর numeric **AppID** এবং 32-byte **ServerSecret** সংগ্রহ করো।
3. Firebase project `messasing-app-9c367`-এ Cloud Functions চালানোর উপযুক্ত billing plan নিশ্চিত করো।
4. PC-তে Node.js 20 ও Firebase CLI install করো: `npm install -g firebase-tools`।
5. `firebase login` চালাও।
6. Repository root-এ `firebase functions:secrets:set ZEGO_SERVER_SECRET --project messasing-app-9c367` চালিয়ে secure prompt-এ secret paste করো।
7. `firebase deploy --only functions:issueZegoCallToken --project messasing-app-9c367` চালাও। প্রথম deploy-এ `ZEGO_APP_ID` চাইলে শুধু numeric AppID দাও।

ServerSecret আমাকে message-এ পাঠাবে না এবং কোনো source file-এ লিখবে না।

## Build note

Low-token নির্দেশনা অনুযায়ী Android/Functions build বা deployment চালানো হয়নি। Targeted contract review ও `git diff --check` করা হয়েছে।
