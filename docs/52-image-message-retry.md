# Step 52 - Image Message Retry

## Goal

Let users retry a failed image message upload without opening the image picker again.

## What Changed

- Added `failedImageUri` to `ChatDetailUiState`.
- Cleared `failedImageUri` before a new text/image send starts.
- Stored the selected image URI when Cloudinary upload fails.
- Added `ChatDetailViewModel.retryFailedImageMessage()`.
- Updated the chat error retry action to retry the failed image when one exists.
- Updated the message input placeholder while sending or after an image failure.

## Verification

1. Open a real conversation.
2. Turn off emulator/device internet.
3. Pick an image and send it.
4. Confirm an error appears.
5. Turn internet back on.
6. Tap `Retry`.
7. Confirm the same image uploads and appears in the chat.
8. Run `./gradlew.bat assembleDebug`.

## Scope

This is a direct retry foundation. It does not yet add upload percentage progress, thumbnail preview while uploading, background retry, or WorkManager.
