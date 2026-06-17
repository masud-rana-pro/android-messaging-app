# Profile Photo Foundation

## Goal

Add the first profile photo upload and preview foundation.

## What Changed

- Added the first media upload dependency path.
- Added Coil Compose dependency for image preview/loading.
- Added `photoUrl` to `UserProfile`.
- Added `ProfilePhotoRepository`.
- Added `CloudinaryProfilePhotoRepository` as the current real implementation.
- Added `FakeProfilePhotoRepository`.
- Added `ProfilePhotoResult`.
- Profile setup screen can pick an image through Android Photo Picker.
- Selected/existing profile photo is previewed in the circular avatar.
- Saving profile uploads the selected image to Cloudinary before saving profile data.
- Profile documents now store `photoUrl`.
- Firestore rules now validate `photoUrl` as a string.

## Current Media Provider

```text
provider: cloudinary
cloud_name: dew95musb
upload_preset: contactme_unsigned
users/{uid}.photoUrl = Cloudinary secure URL
```

## Current Scope

This step supports profile photo upload from the profile setup/edit screen.

It does not yet add:

- profile photo display in every chat/search/avatar surface
- image compression/cropping editor
- remove photo action
- camera capture

## Verification

1. Confirm Cloudinary unsigned preset is enabled.
2. Open profile setup/edit.
3. Tap the circular photo area.
4. Pick an image.
5. Confirm the image preview appears.
6. Save profile.
7. Confirm Cloudinary Media Library receives the image.
8. Confirm Firestore `users/{uid}.photoUrl` is populated with a secure URL.
9. Run `assembleDebug`.
