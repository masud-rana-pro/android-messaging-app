# Step 55 - Image Upload Compression

## Goal

Reduce image upload size before sending profile photos and chat images to Cloudinary.

## What Changed

- Added local image preparation inside `CloudinaryUploadClient`.
- Decodes the selected image into a bitmap when Android can read it.
- Downscales images whose largest side is above 1600 px.
- Compresses prepared images as JPEG at quality 82.
- Uses the compressed image only when it is smaller than the original.
- Keeps the original bytes when compression would not help.
- Stores the actual uploaded MIME type in Firestore metadata.

## Why This Helps

Large images slow down upload, waste bandwidth, and consume more Cloudinary quota. A 1600 px JPEG is enough for current profile photos and chat image previews.

## Verification

1. Upload a normal profile photo.
2. Send a normal chat image.
3. Confirm both appear normally.
4. Confirm Firestore image message `mimeType` remains valid.
5. Run `./gradlew.bat assembleDebug`.

## Scope

This is lightweight foreground compression. It does not yet preserve EXIF orientation manually, show upload percentages, or run background compression jobs.
