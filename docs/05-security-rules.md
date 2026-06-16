# Security Rules Plan

The local Firestore rules draft lives here:

```text
firebase/firestore.rules
```

## Implemented MVP Rules

- Every protected read/write requires `request.auth.uid`.
- A user can create/update only their own `users/{uid}` profile.
- Signed-in users can read public user profiles for discovery.
- Username reservations live in `usernames/{username}`.
- Username reservation create/update/delete is owner-protected.
- Direct conversations require exactly two participants.
- Conversations can only be read/updated by participants.
- Conversation participants can read/create messages.
- Message `senderId` must equal `request.auth.uid`.
- Text messages must be non-empty and no longer than 4000 characters.
- Message update/delete is denied for now.

## Deploy

```bash
firebase deploy --only firestore:rules,firestore:indexes
```

or run:

```bash
scripts/firebase_deploy.sh
```

## Next Security Enhancements

- Add Firebase Emulator Suite tests for existing profile, username, conversation, and message rules.
- Add stricter username validation.
- Add stricter profile field validation.
- Add blocked-user enforcement before messaging/calling.
- Add report creation rules.
- Add Storage rules for profile photos and chat media.
- Add group membership and role-based rules.
- Add call document validation.
- Add rate limiting through Cloud Functions or server-side controls where Firestore rules are not enough.

## Security Principles

- Do not trust Android client data without rules validation.
- Do not expose FCM server keys or Zego secrets in the app.
- Do not show delivered/read receipt unless backed by real recipient-side data.
- Do not claim end-to-end encryption until cryptography, key storage, and message/media encryption are implemented and tested.
