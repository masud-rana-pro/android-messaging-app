# Database Schema

This schema separates currently implemented collections from planned WhatsApp-like features.

## Implemented Firestore Collections

```text
users/{uid}
usernames/{username}
conversations/{conversationId}
conversations/{conversationId}/messages/{messageId}
```

## Current User Profile Model

```text
users/{uid}
  uid
  phoneNumber
  email
  displayName
  username
  photoUrl
  profileComplete
  primaryAuthProvider
  createdAt
  updatedAt
```

Phone number is the primary identity for WhatsApp-like discovery. Email is optional fallback login identity.

## Current Conversation Model

```text
conversations/{conversationId}
  type: "direct"
  participantIds: [uidA, uidB]
  lastMessageText
  lastMessageSenderId
  readAtByUser.{uid}
  createdAt
  updatedAt
```

## Current Message Model

```text
conversations/{conversationId}/messages/{messageId}
  senderId
  type: "text"
  text
  status: "sent"
  createdAt
```

Only text messages are implemented now. Delivered/read status should not be shown until real recipient state exists.

## Planned Firestore Collections

```text
user_private/{uid}
user_devices/{uid}/devices/{deviceId}
contacts/{uid}/items/{contactUid}
blocked_users/{uid}/items/{blockedUid}
reports/{reportId}
groups/{groupId}
groups/{groupId}/members/{uid}
communities/{communityId}
communities/{communityId}/groups/{groupId}
status/{uid}/items/{statusId}
channels/{channelId}
channels/{channelId}/posts/{postId}
calls/{callId}
```

## Planned Realtime Database Paths

```text
presence/{uid}
typing_fast/{conversationId}/{uid}
call_ringing/{receiverUid}/{callId}
```

## Planned Storage Paths

```text
profile_photos/{uid}/profile.jpg
chat_media/{conversationId}/{messageId}/file
status_media/{uid}/{statusId}/file
channel_media/{channelId}/{postId}/file
```

## Schema Rules

- Conversations should own messages as subcollections.
- Media metadata belongs in message documents; binary files belong in Storage.
- Direct conversation documents must contain exactly two participants.
- Group membership and permissions must be modeled before group messages.
- Block/report data must exist before enforcing blocked-user restrictions.
