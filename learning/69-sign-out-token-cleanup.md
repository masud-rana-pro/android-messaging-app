# ধাপ ৬৯: Sign-out করলে device token cleanup

## সমস্যা কী ছিল

FCM token `user_devices/{uid}/devices/{deviceId}`-এ সংরক্ষিত থাকে। User logout করার পর document রেখে দিলে backend আগের account-এর message notification সেই device-এ পাঠাতে পারে। এটি privacy সমস্যা, বিশেষ করে একই ফোন অন্য কেউ ব্যবহার করলে।

## সঠিক logout order

1. বর্তমান authenticated user ID নেওয়া হয়।
2. Firestore থেকে শুধু এই device-এর document delete করা হয়।
3. `FirebaseMessaging.deleteToken()` local token invalidate করে।
4. এরপর `FirebaseAuth.signOut()` session শেষ করে।

Auth আগে sign-out করলে Firestore security rule অনুযায়ী device document delete করার permission আর থাকবে না। তাই cleanup অবশ্যই auth session শেষ হওয়ার আগে হয়।

## কেন সব device delete করা হয়নি

একজন user একই account ফোন ও tablet-এ চালাতে পারেন। এই ফোন থেকে logout করলে শুধু বর্তমান `ANDROID_ID` document delete হয়; অন্য device-এর notification সচল থাকে।

## Cleanup fail হলেও logout কেন হয়

Network বন্ধ থাকলে delete ব্যর্থ হতে পারে। তবুও user-কে logout থেকে আটকানো ঠিক নয়। Backend invalid-token cleanup পরে stale FCM token সরাতে পারে। Production hardening-এ retry queue যোগ করা যাবে।

## `resetSyncState()` কেন প্রয়োজন

`DeviceTokenSyncViewModel` একই user-এর token বারবার write ঠেকাতে user ID cache করে। Logout-এর পর cache reset না করলে একই account আবার login করলেও fresh token sync হতো না। তাই successful sign-out navigation-এর আগে cache `null` করা হয়।

## যাচাই

1. Login করে Firebase Console-এ `user_devices/{uid}/devices` document দেখুন।
2. Settings থেকে logout করুন।
3. Document delete হয়েছে কি না দেখুন।
4. আবার login করলে নতুন token document তৈরি হচ্ছে কি না যাচাই করুন।
