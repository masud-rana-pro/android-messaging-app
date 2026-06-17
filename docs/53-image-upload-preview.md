# Step 53 - Image Upload Preview

## Goal

Show a local preview for the selected photo while it is uploading and after an upload failure.

## What Changed

- Added `pendingImageUri` to `ChatDetailUiState`.
- Set `pendingImageUri` when an image upload starts.
- Cleared `pendingImageUri` after success or failure.
- Reused `failedImageUri` to show the failed photo preview.
- Added `PendingImagePreview` in the chat input area.
- Shows:
  - `Sending photo` with a spinner during upload.
  - `Photo not sent` after failure, with retry guidance.

## Verification

1. Open a real conversation.
2. Pick an image.
3. Confirm a small local preview appears while upload is running.
4. Turn off internet and send an image.
5. Confirm the failed image preview remains visible.
6. Turn internet on and tap retry.
7. Confirm the preview clears after the image appears in the chat.
8. Run `./gradlew.bat assembleDebug`.

## Scope

This is still foreground UI state. It does not create fake Firestore messages, background upload records, or WorkManager retry jobs.
