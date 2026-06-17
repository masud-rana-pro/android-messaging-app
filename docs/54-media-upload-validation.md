# Step 54 - Media Upload Validation

## Goal

Validate selected image files before uploading them to Cloudinary, and show clearer user-facing messages for invalid media.

## What Changed

- Added `MediaUploadException` with a safe user-facing message.
- Added Cloudinary upload validation:
  - image MIME type only
  - maximum 10 MB file size
  - unavailable file detection
- Chat image send now surfaces upload validation messages.
- Profile photo upload now surfaces upload validation messages.

## Validation Rules

```text
allowed MIME: image/*
max size: 10 MB
```

## Verification

1. Upload a normal profile photo and confirm success.
2. Send a normal chat image and confirm success.
3. Try a file larger than 10 MB and confirm the app shows `Choose a photo smaller than 10 MB.`
4. Try a non-image file if the picker/browser allows it and confirm the app shows `Only image files can be uploaded here.`
5. Run `./gradlew.bat assembleDebug`.

## Scope

This is client-side validation for MVP usability. Production hardening should also enforce upload constraints in the Cloudinary unsigned preset and later a signed backend upload flow.
