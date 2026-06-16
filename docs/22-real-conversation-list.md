# Real Conversation List

ContactMe now loads real direct conversations in the Chats tab.

## Flow

```text
Home Chats
-> observe conversations where participantIds contains current user
-> show other user's display name
-> show lastMessageText
-> tap row
-> open Chat Detail with conversationId
```

## Conversation Preview

```text
conversationId
otherUserId
title
subtitle
updatedAtMillis
```

`subtitle` currently comes from:

```text
lastMessageText
```

## Current Limitations

- No unread count yet.
- No message timestamp UI yet.
- Other user profiles are loaded per conversation.
- Demo chat rows still appear when there are no real conversations.

## Verify

1. Search a user and open a conversation.
2. Send a message.
3. Go back Home.
4. Chats tab should show the real conversation with last message.
5. Tap the row and confirm Chat Detail opens the same conversation.
