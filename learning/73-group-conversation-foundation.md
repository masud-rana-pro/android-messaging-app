# ধাপ ৭৩: Group conversation foundation

## এই foundation কেন আগে দরকার

Group UI বানানোর আগে database contract ঠিক না করলে direct chat-এর field ও group field মিশে যায়। এখন conversation document স্পষ্টভাবে `direct` অথবা `group` type বহন করে এবং list renderer type অনুযায়ী title/photo নির্ধারণ করে।

## Group document

- `type`: `group`
- `title`: group name
- `photoUrl`: group photo, শুরুতে খালি
- `participantIds`: creatorসহ সব member
- `adminIds`: শুরুতে creator
- `createdBy`: original creator
- `createdAt`, `updatedAt`: server timestamp

## Validation

Creator ছাড়া অন্তত দুই contact দরকার, তাই minimum group size ৩। Duplicate member ID বাদ যায় এবং সর্বোচ্চ ২৫৬ participant রাখা যায়। Title trim করার পর ১ থেকে ১০০ character হতে হবে। Pure validator-এর তিনটি unit test আছে।

## Preview mapping

Direct chat অন্য user-এর profile থেকে title/photo নেয়। Group chat conversation document-এর নিজের `title` ও `photoUrl` নেয়। এই distinction না থাকলে group list-এ প্রথম member-এর নাম ভুলভাবে group name হিসেবে দেখা যেত।

## Security rules

Signed-in creator group তৈরি করতে পারেন যদি তিনি participant ও admin হন। সাধারণ member message/read metadata update করতে পারেন, কিন্তু group title/photo/admin list শুধু existing admin update করতে পারেন। Participant list পরিবর্তনের dedicated admin flow পরে যোগ হবে।

## Real test-এর আগে

Updated Firestore rules deploy করতে হবে। Android unit test ও APK build local code verify করে, কিন্তু deployed Firebase project পুরোনো rules ব্যবহার করলে group create deny হবে।
