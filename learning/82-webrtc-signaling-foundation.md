# ধাপ ৮২: Free WebRTC signaling foundation

## সংশোধনের কারণ

আগের repository numbering-এ Step 82 message editing ছিল, কিন্তু approved free/no-card PDF plan-এ Step 82 হলো WebRTC call signaling। Audit-এ call session, offer/answer এবং ICE candidate repository অনুপস্থিত পাওয়া যায়। এই checkpoint numbering ও implementation দুটোকে PDF plan-এর সঙ্গে align করেছে।

## Firestore শুধু signaling

Audio/video Firestore দিয়ে যায় না। Firestore শুধু দুই peer-কে connection information আদান-প্রদান করায়:

1. caller `calls/{callId}` এবং SDP offer একই atomic write-এ তৈরি করে;
2. receiver SDP answer, accepted status ও accepted timestamp একই atomic write-এ লেখে;
3. দুই পক্ষ নিজ নিজ ICE candidate subcollection-এ candidate যোগ করে;
4. WebRTC STUN/TURN ব্যবহার করে media path তৈরি করে।

## Call fields

Call document-এ `callerId`, `receiverId`, `type`, `status`, `offer`, `answer`, `createdAt`, `acceptedAt`, `endedAt` আছে। Status values: ringing, accepted, rejected, ended, missed, cancelled, timeout এবং busy।

## Security rules

শুধু caller call document তৈরি করতে পারে। Caller offer লেখে, receiver answer লেখে। Receiver accept/reject/busy করতে পারে; caller cancel করতে পারে; accepted call দুই পক্ষ end করতে পারে। Call identity ও creation timestamp update-এর পরে বদলানো যায় না। Candidate collection-এ caller/receiver শুধু নিজের candidate append করতে পারে।

## সীমা

এটি signaling foundation—call screen, ringing lifecycle, FCM call push বা full audio/video flow Step 89+ ছাড়া implement করা হয়নি।
