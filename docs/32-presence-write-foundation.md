# Presence Write Foundation

## Goal

Add the first Firebase Realtime Database presence foundation so ContactMe can mark the current user online/offline.

## What Changed

- Added Firebase Realtime Database dependency.
- Added `FirebaseDatabase` provider.
- Added `PresenceRepository`.
- Added `FirebasePresenceRepository`.
- Added `FakePresenceRepository`.
- Added Hilt binding in `PresenceModule`.
- Added `PresenceViewModel`.
- `ContactMeApp` now marks the signed-in user:
  - online when app lifecycle starts
  - offline when app lifecycle stops
  - online again when root screen changes after auth/session restore
- Updated `firebase/database.rules.json` for scoped presence reads/writes.
- Added root `firebase.json` so Firebase CLI can find the Firestore, Realtime Database, and Storage rules files.
- Updated `scripts/firebase_deploy.sh` to deploy Realtime Database rules with Firestore rules.

## Realtime Database Shape

```text
presence/{uid}
  isOnline
  lastSeenAt
```

## Why Realtime Database

Presence is a fast, ephemeral realtime feature. Realtime Database supports `onDisconnect()`, which lets Firebase mark a user offline if the app disconnects unexpectedly.

## Current Scope

This step only writes the current user's presence state.

The next presence step should observe the other participant and replace placeholder header text such as `last seen recently`.

## Verification

1. Enable Firebase Realtime Database in the Firebase project.
2. Deploy database rules.
3. Run the app and sign in.
4. Confirm `presence/{uid}/isOnline` becomes `true`.
5. Background/close the app.
6. Confirm `presence/{uid}/isOnline` becomes `false` and `lastSeenAt` updates.
7. Run `assembleDebug`.

Deploy command:

```bash
scripts/firebase_deploy.sh
```
