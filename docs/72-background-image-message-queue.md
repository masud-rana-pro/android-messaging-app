# Step 72: Background Image Message Queue

## Goal

Keep image-message upload alive across screen changes, temporary network loss, and app backgrounding.

## Implementation

- Added WorkManager with Hilt worker injection.
- Copies the selected photo into app-private pending storage before queueing.
- Requires network connectivity and retries failed work up to three attempts with exponential backoff.
- Sends the real Cloudinary image and Firestore message through the existing repository.
- Deletes the private pending file after success and keeps it after final failure for manual retry.
- Observes WorkInfo to update chat preview, success, and failure UI.

## Verification

Run `./gradlew testDebugUnitTest assembleDebug`. Then send a photo with network disabled, restore network, and confirm it uploads without selecting the photo again.

## Commit

`feat(media): queue image messages with WorkManager`
