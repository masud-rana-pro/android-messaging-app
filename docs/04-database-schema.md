# Database Schema

## Firestore Collections

- `users/{uid}`
- `user_private/{uid}`
- `user_devices/{uid}/devices/{deviceId}`
- `contacts/{uid}/items/{contactUid}`
- `conversations/{conversationId}`
- `conversations/{conversationId}/messages/{messageId}`
- `conversations/{conversationId}/typing/{uid}`
- `groups/{groupId}`
- `groups/{groupId}/members/{uid}`
- `communities/{communityId}`
- `communities/{communityId}/groups/{groupId}`
- `status/{uid}/items/{statusId}`
- `channels/{channelId}`
- `channels/{channelId}/posts/{postId}`
- `calls/{callId}`
- `reports/{reportId}`
- `blocked_users/{uid}/items/{blockedUid}`

## User Profile Model

```text
users/{uid}
  uid
  phoneNumber
  email
  displayName
  username
  photoUrl
  primaryAuthProvider
  createdAt
  updatedAt
  lastSeenAt
```

`phoneNumber` is the preferred identity field for WhatsApp-like discovery. `email` is optional fallback identity.

## Realtime Database

- `presence/{uid}`
- `call_ringing/{receiverUid}/{callId}`
- `typing_fast/{conversationId}/{uid}`

## Storage

- `profile_photos/{uid}/profile.jpg`
- `chat_media/{conversationId}/{messageId}/file`
- `status_media/{uid}/{statusId}/file`
- `channel_media/{channelId}/{postId}/file`
