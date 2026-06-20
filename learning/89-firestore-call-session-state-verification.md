# ধাপ ৮৯: Firestore call session/state verification

## কেন atomic write দরকার

Call document আগে তৈরি করে পরে offer লিখলে receiver মাঝের অসম্পূর্ণ ringing state দেখতে পারে। একইভাবে answer এবং accepted status আলাদা write হলে caller accepted state পেলেও answer না-ও পেতে পারে। তাই `createCallOffer` session+offer একসঙ্গে এবং `acceptCallWithAnswer` answer+status+acceptedAt একসঙ্গে লেখে।

## Model mapping

Firestore document ID হলো `callId`। Document fields হলো callerId, receiverId, type, status, offer, answer, createdAt, acceptedAt এবং endedAt। Kotlin mapper server timestamp-গুলো millisecond value-এ রূপান্তর করে। Audio/video এবং আটটি status একই enum mapping ব্যবহার করে।

## Incoming ও current-call listener

Incoming listener শুধু `receiverId` query করে এবং client-side ringing filter ব্যবহার করে; এতে অতিরিক্ত composite index লাগে না। Firestore rules query-কে authenticated receiver-এর data-তেই সীমাবদ্ধ রাখে। Single-call listener document state live observe করে।

## ICE ownership

Caller candidate শুধু `callerCandidates`, receiver candidate শুধু `receiverCandidates` collection-এ যায়। দুই participant উভয় collection পড়তে পারে, কিন্তু নিজের role-এর collection ছাড়া লিখতে পারে না। Candidate update/delete নিষিদ্ধ।

## State transition rules

- ringing → accepted: receiver, answer ও acceptedAt একই write;
- ringing → rejected/busy: receiver;
- ringing → cancelled: caller;
- ringing → missed/timeout: participant;
- accepted → ended: caller বা receiver।

Identity, type ও createdAt immutable। Offer creation-এর পরে immutable এবং answer শুধু atomic acceptance-এ লেখা যায়। Existing messaging/user rules পরিবর্তন করা হয়নি।

## Scope boundary

এই step repository/state verification পর্যন্ত। Outgoing media orchestration, microphone track, call UI, incoming screen, video, Worker/FCM এবং Step 90 implementation করা হয়নি। Full Gradle build/test-ও limit-saving নির্দেশনায় চালানো হয়নি।
