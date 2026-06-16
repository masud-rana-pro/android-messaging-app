# Profile Photo Foundation

## Goal

Add the first profile photo upload and preview foundation.

## What Changed

- Added Firebase Storage dependency.
- Added Coil Compose dependency for image preview/loading.
- Added `FirebaseStorage` provider.
- Added `photoUrl` to `UserProfile`.
- Added `ProfilePhotoRepository`.
- Added `FirebaseProfilePhotoRepository`.
- Added `FakeProfilePhotoRepository`.
- Added `ProfilePhotoResult`.
- Profile setup screen can pick an image through Android Photo Picker.
- Selected/existing profile photo is previewed in the circular avatar.
- Saving profile uploads the selected image to Firebase Storage before saving profile data.
- Profile documents now store `photoUrl`.
- Firestore rules now validate `photoUrl` as a string.
- Storage rules allow signed-in users to read profile photos and only the owner to upload their own `profile.jpg`.
- Firebase deploy script now deploys Storage rules too.

## Storage Path

```text
profile_photos/{uid}/profile.jpg
```

## Current Scope

This step supports profile photo upload from the profile setup/edit screen.

It does not yet add:

- profile photo display in every chat/search/avatar surface
- image compression/cropping editor
- remove photo action
- camera capture

## Verification

1. Deploy Firebase rules with `scripts/firebase_deploy.sh`.
2. Open profile setup/edit.
3. Tap the circular photo area.
4. Pick an image.
5. Confirm the image preview appears.
6. Save profile.
7. Confirm Firebase Storage contains `profile_photos/{uid}/profile.jpg`.
8. Confirm Firestore `users/{uid}.photoUrl` is populated.
9. Run `assembleDebug`.
