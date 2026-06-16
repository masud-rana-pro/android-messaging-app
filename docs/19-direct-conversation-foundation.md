# Direct Conversation Foundation

ContactMe now creates or reuses a one-to-one conversation when a discovered user is opened.

## Flow

```text
Chats
-> search username
-> tap discovered user
-> get/create conversations/{conversationId}
-> open Chat Detail
```

## Conversation Id

Direct conversation ids are deterministic:

```text
sorted(currentUserId, otherUserId).join("__")
```

This prevents duplicate one-to-one conversations for the same two users.

## Firestore Document

```text
conversations/{conversationId}
```

Fields:

```text
type: direct
participantIds: [uid1, uid2]
participantKey: uid1__uid2
createdAt: server timestamp
updatedAt: server timestamp
```

## Current Limitations

- Messages are still placeholder UI.
- Conversation list does not load from Firestore yet.
- Chat Detail receives the conversation id but does not read/write messages yet.

## Next Step

Add real message sending:

```text
conversations/{conversationId}/messages/{messageId}
```
