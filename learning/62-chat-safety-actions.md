# Step 62 - Chat Safety Actions

এই ধাপে আমরা chat detail screen-এ basic `Report` এবং `Block` action যোগ করেছি।

## আগের step-এ কী ছিল?

আগের step-এ block/report data foundation ছিল:

```text
SafetyRepository
blocked_users
reports
Firestore rules
send/open enforcement
```

কিন্তু user app থেকে button tap করে block/report করতে পারত না।

## এই step-এ কী হলো?

Chat header-এ দুইটা action যোগ হয়েছে:

```text
Report
Block
```

## Peer user id কীভাবে পাওয়া হচ্ছে?

Chat screen সবসময় peer user id জানে না। তাই UI peer id pass করছে না।

Repository conversation document থেকে participant list পড়ে peer user বের করে:

```text
conversations/{conversationId}.participantIds
```

তারপর current user বাদ দিয়ে peer user id নেয়।

## Block flow

```text
Block tap
-> ChatDetailViewModel.blockCurrentChat()
-> SafetyRepository.blockConversationPeer()
-> blocked_users/{currentUid}/items/{peerUid}
-> input disabled
```

Block success হলে message input disabled হয় এবং user আর message পাঠাতে পারে না।

## Report flow

```text
Report tap
-> ChatDetailViewModel.reportCurrentChat()
-> SafetyRepository.reportConversationPeer()
-> reports/{reportId}
```

এখন default reason:

```text
other
```

পরে reason picker যোগ করা যাবে।

## UI state

`ChatDetailUiState`-এ যোগ হয়েছে:

```kotlin
isSafetyActionInProgress
isChatBlocked
statusMessage
```

এগুলো দিয়ে button disable, input disable, success message দেখানো হয়।

## কীভাবে verify করবে?

1. Real chat open করো।
2. `Report` tap করো।
3. Firestore `reports` collection-এ document এসেছে কিনা দেখো।
4. `Block` tap করো।
5. Firestore `blocked_users/{uid}/items/{peerUid}` document এসেছে কিনা দেখো।
6. Input field `Chat unavailable` দেখায় কিনা দেখো।

## শেখার বিষয়

Safety feature UI দিয়ে শুরু করলেও backend enforcement ছাড়া incomplete। এখানে UI action real data লিখছে, আর আগের step-এর enforcement send/open flow বন্ধ করছে।
