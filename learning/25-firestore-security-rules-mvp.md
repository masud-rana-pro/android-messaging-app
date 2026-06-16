# Step 25: Firestore Security Rules MVP

এই ধাপে ContactMe app-এর current Firestore features অনুযায়ী local security rules draft update করা হয়েছে।

## কেন এই step দরকার

App-এ এখন Firestore ব্যবহার হচ্ছে:

```text
users
usernames
conversations
messages
```

যদি Firebase Console-এ rules সব deny থাকে, তাহলে app runtime-এ error দেবে:

```text
We could not save your profile. Please try again.
We could not open this chat. Please try again.
We could not send this message. Please try again.
```

তাই local rules file update করা হলো।

## কোন files change হয়েছে

```text
firebase/firestore.rules
docs/05-security-rules.md
docs/25-firestore-rules-mvp.md
learning/25-firestore-security-rules-mvp.md
scripts/firebase_deploy.sh
```

## `firebase/firestore.rules`

এই file-এ Firestore access control লেখা থাকে।

আগে ছিল:

```text
allow read, write: if false;
```

মানে সব denied।

এখন implemented MVP অনুযায়ী scoped rules আছে।

## Helper functions

```text
isSignedIn()
isOwner(userId)
isConversationParticipant(conversationId)
```

কেন:

- একই logic বারবার লিখতে হয় না
- rules readable থাকে
- ভুল কম হয়

## `users/{userId}`

Rule:

```text
allow read: signed in users
allow create/update: only owner
```

কেন read signed-in users:

- user discovery/search করতে অন্য user profile name/username পড়তে হয়
- public profile data হিসেবে displayName/username রাখা হয়েছে

কেন write only owner:

- কেউ অন্যের profile edit করতে পারবে না

## `usernames/{username}`

Rule:

```text
create/update/delete: only username owner
```

কেন:

- username unique রাখার জন্য reservation document লাগে
- অন্য user যেন username hijack করতে না পারে

## `conversations/{conversationId}`

Rule:

```text
read/update: participants only
create: direct conversation with exactly 2 participants and current user included
```

কেন:

- অন্য user-এর chat list পড়া যাবে না
- direct chat create করতে current user participant হতে হবে

## `messages/{messageId}`

Rule:

```text
read/create: conversation participants only
senderId must equal request.auth.uid
type must be text
text length 1..4000
update/delete denied
```

কেন senderId check:

- user অন্য কারো নামে message পাঠাতে পারবে না

কেন update/delete denied:

- edit/delete message feature এখনো implement হয়নি
- feature না থাকলে rule deny রাখা safer

## Deploy command

```bash
firebase deploy --only firestore:rules,firestore:indexes
```

Script:

```bash
scripts/firebase_deploy.sh
```

## কীভাবে verify করবে

1. Firebase CLI login/setup থাকতে হবে।
2. project select করা থাকতে হবে।
3. deploy চালাও:

```bash
firebase deploy --only firestore:rules,firestore:indexes
```

4. app থেকে profile save করো।
5. username search করো।
6. conversation open করো।
7. message send করো।

সব কাজ করলে rules current MVP flow allow করছে।

## এখনো কী বাকি

- Firebase Emulator Suite rules tests
- stricter username regex validation
- blocked users rule
- media upload rules
- reports/moderation rules
- group/community/channel rules

## পরের step

```text
Unread count foundation
or
Firestore Emulator rules tests
```
