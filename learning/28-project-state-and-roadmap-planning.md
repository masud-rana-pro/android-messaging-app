# Step 28: Project State and Roadmap Planning

এই step-এ আমরা app code বদলাইনি। আমরা roadmap/documentation update করেছি, যাতে project এখন কোথায় আছে এবং এখান থেকে কীভাবে এগোবে সেটা পরিষ্কার থাকে।

## 1. কেন roadmap update দরকার ছিল?

পুরনো Word roadmap-এ পুরো WhatsApp-like app-এর বড় vision ছিল। কিন্তু project এখন অনেকটা এগিয়েছে:

- Firebase Auth হয়েছে
- Phone OTP flow হয়েছে
- Profile Firestore-এ save হচ্ছে
- Username discovery হয়েছে
- Direct conversation হচ্ছে
- Real text message send/render হচ্ছে
- Conversation list real data থেকে আসছে
- Unread/read foundation হয়েছে
- Message status foundation হয়েছে

তাই পুরনো docs-এ যদি এখনও লেখা থাকে “Real Firebase Auth not implemented” বা “Message send/receive not implemented”, তাহলে সেটা ভুল context তৈরি করবে।

Roadmap update করার মূল কারণ:

1. বর্তমান state পরিষ্কার করা।
2. কোন feature done, partial, not started তা আলাদা করা।
3. পরের implementation step random না করে roadmap অনুযায়ী করা।
4. শেখার file আর technical docs একসাথে sync রাখা।

## 2. Word roadmap-এর কাজ কী?

File:

`docs/ContactMe_Full_App_Roadmap.docx`

এই fileটা polished master roadmap। Portfolio বা high-level planning দেখানোর জন্য এটা ভালো।

এখানে থাকবে:

- Product goal
- Current checkpoint
- Full WhatsApp-like feature map
- Firebase-first architecture
- Phase roadmap
- Git workflow
- Learning workflow
- Release plan

## 3. Markdown docs-এর কাজ কী?

Markdown docs implementation source হিসেবে থাকবে। মানে কাজ করার সময় আমরা এগুলো বেশি follow করব।

Important docs:

```text
docs/01-product-roadmap.md
docs/02-feature-specification.md
docs/03-architecture.md
docs/04-database-schema.md
docs/05-security-rules.md
docs/06-notification-flow.md
docs/07-calling-flow.md
docs/08-release-checklist.md
docs/09-android-system-components.md
docs/28-current-state-and-next-roadmap.md
```

## 4. `01-product-roadmap.md` কেন update করা হলো?

আগে এটা শুধু এক লাইনের staged roadmap ছিল। এখন এতে current checkpoint এবং future release order আছে।

এটা দেখে বোঝা যাবে:

- এখন পর্যন্ত কী হয়েছে
- এরপর কোন phase
- কোন release-এ কী থাকবে
- default product decisions কী

## 5. `02-feature-specification.md` কেন update করা হলো?

আগে এই file-এ অনেক feature “not implemented” হিসেবে ছিল, যেগুলো এখন আংশিক বা fully implemented।

এখন এখানে তিনটা ভাগ আছে:

1. Implemented foundation
2. Current partial areas
3. Not implemented yet

এতে আমরা ভুল করে finished feature আবার plan করব না।

## 6. `03-architecture.md` কেন গুরুত্বপূর্ণ?

Architecture doc বলে app কোন stack দিয়ে বানানো হচ্ছে।

Current decision:

- Android only
- Firebase-first backend
- Compose UI
- Hilt dependency injection
- Repository pattern
- Firestore realtime listeners
- Future: Storage, FCM, Cloud Functions, Realtime Database, ZegoCloud

এটা জানলে implementation-এর সময় random library বা random architecture ঢুকে যাবে না।

## 7. `04-database-schema.md` কেন আলাদা করে current/planned করা হলো?

Database schema আগে সব future collection একসাথে দেখাত। এখন current আর planned আলাদা।

Current:

```text
users/{uid}
usernames/{username}
conversations/{conversationId}
conversations/{conversationId}/messages/{messageId}
```

Planned:

```text
user_devices
blocked_users
reports
groups
status
channels
calls
```

এই separation দরকার, কারণ rules, UI, repository সব current schema অনুযায়ী কাজ করবে। Future schema আগে থেকে জানা থাকবে, কিন্তু একসাথে implement করা হবে না।

## 8. `05-security-rules.md` কেন update করা হলো?

Security rules এখন MVP flow allow করে:

- user নিজের profile লিখতে পারে
- signed-in user profile পড়তে পারে
- username owner-protected
- direct conversation participant-only
- message sender auth user হতে হবে

কিন্তু এখনো অনেক gap আছে:

- media rules
- blocked user enforcement
- group roles
- call validation
- report rules
- Emulator Suite tests

তাই doc-এ current rules আর next security enhancements আলাদা করা হয়েছে।

## 9. Notification আর Calling docs কেন detailed হলো?

Notification/call feature এখনও implement হয়নি, কিন্তু এগুলো complex। আগে plan পরিষ্কার না করলে পরে app code messy হবে।

Notification flow:

1. User message পাঠাবে
2. Firestore document create হবে
3. Cloud Function event ধরবে
4. Receiver device token load করবে
5. FCM notification পাঠাবে
6. Android notification tap করলে exact chat খুলবে

Calling flow:

1. Caller call document create করবে
2. Receiver ringing state পাবে
3. FCM incoming call notification যাবে
4. Receiver accept/reject করবে
5. ZegoCloud media session handle করবে
6. Call history save হবে

## 10. Android system components কেন এখনো সব add করা হয়নি?

Android-এ অনেক component আছে:

- ContentProvider
- FileProvider
- BroadcastReceiver
- Service
- Foreground Service
- FirebaseMessagingService
- WorkManager
- Room

কিন্তু এগুলো সব শুরুতেই add করলে app জটিল হবে। তাই rule:

> Feature দরকার না হলে component add করব না।

Example:

- Media না আসা পর্যন্ত FileProvider লাগবে না।
- Notification না আসা পর্যন্ত FirebaseMessagingService লাগবে না।
- Call না আসা পর্যন্ত Foreground Service লাগবে না।
- Offline/cache stable না হওয়া পর্যন্ত Room লাগবে না।

## 11. Step 28 docs কীভাবে future কাজ guide করবে?

File:

`docs/28-current-state-and-next-roadmap.md`

এটা বর্তমান project map।

এখানে আছে:

- project truth
- completed matrix
- partial/not started matrix
- next implementation order
- Git workflow
- verification workflow
- next recommended step

প্রতিবার নতুন কাজ শুরু করার আগে এই file দেখে বুঝব আমরা roadmap-এর কোন জায়গায় আছি।

## 12. আমাদের next implementation কী হওয়া উচিত?

Docs update commit করার পর next recommended feature:

**Chat MVP Finish**

এর মধ্যে থাকবে:

1. navigation state clean করা
2. chat empty/loading/error state improve করা
3. send failure/retry foundation
4. তারপর typing/presence অথবা phone search

## 13. কীভাবে verify করবে?

এই step docs-only। তাই app build জরুরি না, তবে চাইলে চালানো যায়।

Docs verification:

```powershell
git diff --stat
git status --short --branch
```

Word verification:

1. `docs/ContactMe_Full_App_Roadmap.docx` open করো।
2. দেখো current checkpoint আছে কি না।
3. দেখো `apps/ContactMe` path আছে কি না।
4. দেখো Firebase-first Android-only explanation আছে কি না।
5. দেখো phase order current plan অনুযায়ী হয়েছে কি না।

## 14. এই step-এর main learning

একটা বড় app বানানোর সময় code লেখার আগে roadmap sync করা খুব গুরুত্বপূর্ণ।

কারণ:

- outdated docs ভুল decision তৈরি করে
- current state না জানলে duplicate work হয়
- future feature dependency বোঝা যায় না
- security এবং data model আগে থেকে align করা যায়
- learning trace পরিষ্কার থাকে

এই step-এর output হলো: project এখন থেকে A-Z roadmap অনুযায়ী আবার step-by-step এগোনোর জন্য ready।
