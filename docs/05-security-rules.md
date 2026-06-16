# Security Rules Plan

The local Firestore rules draft lives here:

```text
firebase/firestore.rules
```

## Implemented MVP Rules

- Every write validates `request.auth.uid`.
- A user can create/update only their own `users/{uid}` profile.
- Signed-in users can read public user profiles for discovery.
- Username reservations live in `usernames/{username}`.
- A username reservation can only be created/updated/deleted by its owner.
- Direct conversations require exactly two participants.
- A conversation can only be read/updated by participants.
- Only participants can read/write conversation messages.
- `senderId` must equal the authenticated user.
- Text messages are limited to 4000 characters.
- Message update/delete is denied for now.

## Deploy

```bash
firebase deploy --only firestore:rules,firestore:indexes
```

or run:

```bash
scripts/firebase_deploy.sh
```

## Still Planned

- Chat media must be limited to conversation participants.
- Blocked users cannot message or call each other.
- Reports are stored for moderation.
- Stronger username validation in rules.
- Rules tests with Firebase Emulator Suite.
- Do not hardcode FCM server keys or Zego secrets in the client.
- Do not claim E2EE until it is properly implemented and tested.
