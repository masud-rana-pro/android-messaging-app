# Firestore Rules MVP

This step updates the local Firestore rules draft for the currently implemented app features.

## Covered Collections

```text
users/{uid}
usernames/{username}
user_devices/{uid}/devices/{deviceId}
blocked_users/{uid}/items/{blockedUid}
reports/{reportId}
conversations/{conversationId}
conversations/{conversationId}/messages/{messageId}
conversations/{conversationId}/typing/{uid}
calls/{callId}
calls/{callId}/callerCandidates/{candidateId}
calls/{callId}/receiverCandidates/{candidateId}
```

## Current Guarantees

- Only signed-in users can access app data.
- Users can write only their own profile.
- Username reservation documents are owner-protected.
- Direct conversations must include the signed-in user.
- Only conversation participants can read/update conversations.
- Only conversation participants can read/create messages.
- Message sender must be the authenticated user.
- Text message size is capped at 4000 characters.
- Calls and ICE candidates are restricted to participants only.
- Users can see if they are blocked by another user.

## Deploy

```bash
firebase deploy --only firestore:rules,firestore:indexes
```

## Important

These rules are an MVP draft. Before production, add Emulator Suite tests and stricter validation for profile fields, username format, blocked users, media, and reporting flows.
