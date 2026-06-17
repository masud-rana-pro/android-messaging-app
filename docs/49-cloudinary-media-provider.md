# Step 49 - Cloudinary Media Provider

## Goal

Use Cloudinary as the real MVP media provider for profile photos and chat image messages while Firebase Storage is unavailable on the current Firebase free-plan setup.

## What Changed

- Removed the app dependency on Firebase Storage.
- Added OkHttp for direct Cloudinary unsigned uploads.
- Added `CloudinaryUploadClient`.
- Added `CloudinaryUpload` result metadata.
- Replaced `FirebaseProfilePhotoRepository` with `CloudinaryProfilePhotoRepository`.
- Updated image messages to store Cloudinary metadata in Firestore.
- Kept Firebase Auth and Firestore as the app identity and chat source of truth.

## Cloudinary Config

```text
cloud_name: dew95musb
upload_preset: contactme_unsigned
upload endpoint: https://api.cloudinary.com/v1_1/dew95musb/auto/upload
```

The Android app uses only the public cloud name and unsigned preset name. It must never include Cloudinary API secret.

## Current Upload Flow

```text
Android Photo Picker
  -> content Uri
  -> Cloudinary unsigned upload
  -> secure_url and public_id
  -> Firestore profile/message metadata
  -> Compose UI renders image from secure_url
```

## Firestore Metadata

Image messages store:

```text
type: "image"
mediaProvider: "cloudinary"
mediaUrl: Cloudinary secure URL
mediaPublicId: Cloudinary public id
mimeType: selected image MIME type
```

Profile photos store:

```text
users/{uid}.photoUrl = Cloudinary secure URL
```

## Verification Checklist

1. Run `./gradlew.bat assembleDebug`.
2. Confirm Cloudinary preset `contactme_unsigned` is unsigned and enabled.
3. Open profile setup/edit and upload a profile photo.
4. Confirm the profile photo appears and `users/{uid}.photoUrl` is updated.
5. Open a real chat and send an image.
6. Confirm the image appears in the chat.
7. Confirm Cloudinary Media Library contains the uploaded assets.
8. Confirm Firestore message metadata includes `mediaProvider`, `mediaUrl`, `mediaPublicId`, and `mimeType`.

## Next Step

Add upload progress, compression, failure retry, and later a signed upload backend through Cloud Functions before production release.
