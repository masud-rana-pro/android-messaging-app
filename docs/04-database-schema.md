# Database Schema

This schema separates currently implemented collections from planned WhatsApp-like features.

## Implemented Firestore Collections

```text
users/{uid}
usernames/{username}
contacts/{uid}/items/{contactUid}
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

## Current Contact Model

```text
contacts/{uid}/items/{contactUid}
  userId
  displayName
  username
  phoneNumber
  photoUrl
  updatedAt
```

The contact document owner is the user who saved the contact. Privacy checks treat `contacts` visibility as visible only when the viewer exists in the profile owner's contact list.

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
  type: "text" | "image"
  text
  mediaProvider
  mediaUrl
  mediaPublicId
  mimeType
  status: "sent"
  createdAt
```

Text and image messages are implemented now. Image files are uploaded to Cloudinary through the current unsigned MVP preset, and Firestore stores message metadata such as `mediaProvider`, `mediaUrl`, `mediaPublicId`, and `mimeType`.

## Planned Firestore Collections

```text
user_private/{uid}
user_devices/{uid}/devices/{deviceId}
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

## Current Media Provider

```text
provider: cloudinary
cloud_name: dew95musb
upload_preset: contactme_unsigned
profile photo URL: users/{uid}.photoUrl
chat image URL: conversations/{conversationId}/messages/{messageId}.mediaUrl
```

Firebase Storage paths are paused for now because Storage requires billing in the current Firebase project. If the project later moves media back to Firebase Storage, planned paths can be:

- `profile_photos/{uid}/profile.jpg`
- `chat_media/{conversationId}/{messageId}/file`
- `status_media/{uid}/{statusId}/file`
- `channel_media/{channelId}/{postId}/file`

## Schema Rules

- Conversations should own messages as subcollections.
- Media metadata belongs in message documents; binary files currently belong in Cloudinary.
- Direct conversation documents must contain exactly two participants.
- Group membership and permissions must be modeled before group messages.
- Block/report data must exist before enforcing blocked-user restrictions.
