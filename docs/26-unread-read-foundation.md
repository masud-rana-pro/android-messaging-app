# Unread And Read Foundation

ContactMe now stores a lightweight read marker per conversation participant.

## Firestore Field

```text
conversations/{conversationId}
  readAtByUser: {
    uid: timestamp
  }
```

## Flow

```text
Chat Detail opens
-> mark readAtByUser.{currentUid}
Messages change while chat is open
-> mark read again
Home Chats list
-> compare updatedAt with readAtByUser.{currentUid}
-> show unread indicator
```

## Current UI

- Unread conversation title is bold.
- Unread last-message preview is stronger.
- Unread timestamp is green.
- A small green dot appears on unread rows.

## Current Limitations

- No unread numeric count yet.
- Read receipts/ticks are not implemented yet.
- Read marker is conversation-level, not per-message-level.
