# Firestore Rules MVP

This step updates the local Firestore rules draft for the currently implemented app features.

## Covered Collections

```text
users/{uid}
usernames/{username}
conversations/{conversationId}
conversations/{conversationId}/messages/{messageId}
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

## Deploy

```bash
firebase deploy --only firestore:rules,firestore:indexes
```

## Important

These rules are an MVP draft. Before production, add Emulator Suite tests and stricter validation for profile fields, username format, blocked users, media, and reporting flows.
