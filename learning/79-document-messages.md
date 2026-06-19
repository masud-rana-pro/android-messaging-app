# ধাপ ৭৯: PDF ও document message

## সম্পূর্ণ flow

1. Attachment icon থেকে **Document** নির্বাচন করা হয়।
2. Android system picker শুধু PDF, text, DOC ও DOCX দেখায়।
3. নির্বাচিত file app-private `pending_media` folder-এ copy হয়।
4. WorkManager network পাওয়া পর্যন্ত কাজ ধরে রাখে।
5. Worker Cloudinary upload করে এবং Firestore message document লেখে।
6. সফল হলে local pending file delete হয়।
7. তিনবার ব্যর্থ হলে filename ও retry action screen-এ থাকে।

## Picker URI কেন copy করা হয়

System picker-এর URI permission process restart-এর পরে হারাতে পারে। Worker background-এ পরে চালু হলে original URI পড়তে নাও পারে। Private copy worker-কে স্থায়ী readable file দেয় এবং অন্য app এই pending file দেখতে পারে না।

## Allowlist ও size validation

শুধু এই MIME types গ্রহণ করা হয়:

- `application/pdf`
- `text/plain`
- `application/msword`
- DOCX MIME type

সর্বোচ্চ size ২৫ MB। Queue, Cloudinary client এবং Firestore rules—তিন layer-এ সীমা রাখা হয়েছে। UI validation bypass করলেও backend data contract invalid metadata গ্রহণ করবে না।

## WorkManager data

Worker input-এ conversation ID, sender ID, private file path, original filename, MIME type ও file size থাকে। Network constraint ছাড়া upload শুরু হয় না। Exponential retry server-কে দ্রুত repeated request দিয়ে চাপ দেয় না।

## Firestore message fields

- `type: document`
- `mediaUrl`
- `mediaPublicId`
- `mediaProvider: cloudinary`
- `fileName`
- `fileSizeBytes`
- `mimeType`
- sender/status/timestamp

Conversation-এর last-message text হিসেবে filename রাখা হয়, ফলে chat list-এ meaningful preview দেখা যায়।

## Retry metadata

Worker failure-এর পরে local URI-এর content resolver original MIME চিনতে পারে না। তাই queued model filename ও MIME type সংরক্ষণ করে; manual retry সেই metadata আবার queue-তে পাঠায়।

## Document bubble

Bubble-এ document icon, filename ও formatted KB/MB size থাকে। Tap করলে Android `ACTION_VIEW` দিয়ে Cloudinary secure URL খোলে। কোনো compatible app না থাকলে app crash না করার জন্য launch `runCatching`-এর মধ্যে থাকে।

## কীভাবে যাচাই করবেন

1. Updated Firestore rules deploy করুন।
2. PDF এবং DOCX পাঠান।
3. Network বন্ধ রেখে document select করুন, পরে network চালু করুন।
4. Receiver account-এ filename/size এবং notification preview যাচাই করুন।
5. Bubble tap করে file খুলুন।
