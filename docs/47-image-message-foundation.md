# Step 47 - Image Message Foundation

## Goal

Add the first real media messaging path using Cloudinary upload and Firestore message metadata.

## What Changed

- Added `MessageType` with `text` and `image`.
- Extended `ChatMessage` with `type` and `mediaUrl`.
- Added `MessageRepository.sendImageMessage(...)`.
- `FirebaseMessageRepository` now uploads selected images to Cloudinary.
- Image message metadata is stored in Firestore under the real conversation.
- Chat detail screen can pick an image from the Android Photo Picker.
- Chat detail screen renders image messages with `AsyncImage`.
- Removed the old `FakeMessageRepository`.
- Firestore stores Cloudinary media metadata for MVP image messages.

## Cloudinary Upload

```text
cloud_name: dew95musb
upload_preset: contactme_unsigned
provider: cloudinary
```

## Firestore Message Shape

```text
conversations/{conversationId}/messages/{messageId}
  senderId
  type: "image"
  text: ""
  mediaProvider: "cloudinary"
  mediaUrl
  mediaPublicId
  mimeType
  status: "sent"
  createdAt
```

## Verification Checklist

1. Build the app with `./gradlew.bat assembleDebug`.
2. Confirm Cloudinary unsigned preset is enabled.
3. Open a real conversation.
4. Tap the image `+` action in the chat input bar.
5. Pick an image.
6. Confirm the image uploads to Cloudinary and appears in the chat.
7. Confirm the conversation list last message shows `Photo`.

## Next Step

Add progress/thumbnail polish and a signed upload backend later.
