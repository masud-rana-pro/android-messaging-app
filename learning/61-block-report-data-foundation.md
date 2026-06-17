# Step 61 - Block And Report Data Foundation

এই ধাপে আমরা block/report feature-এর real data foundation করেছি।

## এই step-এ UI button যোগ হয়েছে?

না।

এই step-এ data model, repository, rules, এবং chat enforcement হয়েছে। UI action পরের step-এ যোগ করা যাবে।

## Firestore path

Block list:

```text
blocked_users/{uid}/items/{blockedUid}
```

Report:

```text
reports/{reportId}
```

## কেন block আলাদা collection?

এক user অনেক user block করতে পারে। তাই `users/{uid}` document-এর ভিতরে বড় list রাখা ভালো না।

Better:

```text
blocked_users
  uid
    items
      blockedUid
```

## SafetyRepository

নতুন repository:

```text
apps/ContactMe/app/src/main/java/com/contactme/app/safety/SafetyRepository.kt
```

Methods:

```kotlin
blockUser(...)
unblockUser(...)
reportUser(...)
hasBlockBetween(...)
```

## `hasBlockBetween` কেন দরকার?

Messaging app-এ শুধু block document রাখা যথেষ্ট না। Chat open/send করার সময় check করতে হয়:

```text
current user blocked peer?
peer blocked current user?
```

দুইটার যেকোনো একটা true হলে chat unavailable।

## Conversation enforcement

`FirebaseConversationRepository.getOrCreateDirectConversation(...)` এখন block relation check করে।

যদি block থাকে:

```text
This chat is not available.
```

## Message enforcement

`FirebaseMessageRepository` text এবং image message send করার আগে block relation check করে।

এর ফলে existing conversation থাকলেও block করার পর message পাঠানো যাবে না।

## Security rules

Rules যোগ হয়েছে:

```text
blocked_users/{userId}/items/{blockedUserId}
reports/{reportId}
```

User শুধু নিজের block list manage করতে পারবে। Report create করা যাবে, কিন্তু read/update/delete বন্ধ।

## কীভাবে verify করবে?

1. Firestore-এ manual block document create করো।
2. দুই user-এর মধ্যে chat open/send try করো।
3. `This chat is not available.` আসা উচিত।
4. Rules deploy করলে অন্য user-এর block list write করা যাবে না।

## শেখার বিষয়

Block/report শুধু UI না। আসল কাজ হলো data model + rules + send/open enforcement। UI button পরে এলেও backend behavior আগে secure হওয়া দরকার।
