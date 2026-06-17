# Step 63 - Chat Unblock Action

এই ধাপে block flow complete করার জন্য chat screen-এ unblock action যোগ করেছি।

## সমস্যা কী ছিল?

আগের step-এ user `Block` করতে পারত। কিন্তু block করার পরে app থেকে unblock করার কোনো path ছিল না।

Messaging app-এ এটা incomplete UX।

## কী যোগ করা হলো?

Header action এখন state অনুযায়ী বদলায়:

```text
normal chat -> Block
current user block করেছে -> Unblock
peer user block করেছে -> chat unavailable, unblock নেই
```

## কেন দুই ধরনের block state দরকার?

Block relation দুইভাবে হতে পারে:

```text
আমি তাকে block করেছি
সে আমাকে block করেছে
```

আমি যদি তাকে block করি, আমি unblock করতে পারি।

কিন্তু সে যদি আমাকে block করে, আমি তার block remove করতে পারি না।

## Repository helper

`SafetyRepository`-এ যোগ হয়েছে:

```kotlin
unblockConversationPeer(...)
hasCurrentUserBlockedConversationPeer(...)
hasBlockInConversation(...)
```

## Chat open হলে কী হয়?

`ChatDetailViewModel.openConversation(...)` conversation open করার সময় safety state load করে।

Flow:

```text
conversation open
-> hasBlockInConversation
-> hasCurrentUserBlockedConversationPeer
-> UI state update
```

## Unblock flow

```text
Unblock tap
-> SafetyRepository.unblockConversationPeer
-> blocked_users/{currentUid}/items/{peerUid} delete
-> input enabled
```

## কীভাবে verify করবে?

1. Real chat open করো।
2. `Block` tap করো।
3. Input disabled হবে।
4. Button `Unblock` হবে।
5. `Unblock` tap করো।
6. Input আবার enabled হবে।

## শেখার বিষয়

Safety feature-এ শুধু action নয়, ownership বোঝা জরুরি। নিজের block নিজে remove করা যায়, কিন্তু অন্য user-এর block remove করা যায় না।
