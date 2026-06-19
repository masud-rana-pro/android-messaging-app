# Step 79: Document Messages

## Goal

Send and open real PDF, text, DOC, and DOCX messages with durable background delivery.

## Implementation

- Added the `document` message type and Firestore metadata.
- Added a document option to the attachment menu and Android document picker.
- Preserves selected files in app-private storage before queueing.
- Uploads through Cloudinary with a 25 MB limit and explicit MIME allowlist.
- Uses a network-constrained Hilt WorkManager worker with retry.
- Stores file name, size, MIME type, URL, and Cloudinary public ID.
- Shows queued/failed document state and supports retry.
- Renders a document bubble that opens the remote URL.
- Includes document names in conversation previews and notifications.
- Added matching Firestore create validation.

## Verification

Run Firebase Functions build and Android tests/APK build. Deploy Firestore rules before testing a real document send.

## Commit

`feat(chat): add document messages`
