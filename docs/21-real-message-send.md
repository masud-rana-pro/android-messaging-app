# Real Message Send

ContactMe now sends and renders text messages for real direct conversations.

## Firestore Path

```text
conversations/{conversationId}/messages/{messageId}
```

Message fields:

```text
senderId
text
type: text
createdAt
```

Conversation cache fields updated on send:

```text
lastMessageText
lastMessageSenderId
updatedAt
```

## Flow

```text
Chat Detail
-> type message
-> Send
-> write message document
-> update conversation last message
-> realtime listener renders message
```

## Current Limitations

- Conversation list still uses placeholder chats.
- Message timestamps are not displayed yet.
- Message delivery/read status is not implemented yet.
- Media messages are not implemented yet.

## Verify

1. Open a discovered user's real conversation.
2. Type a message.
3. Tap Send.
4. Confirm message appears in Chat Detail.
5. Confirm Firestore has `conversations/{conversationId}/messages`.
