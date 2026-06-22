# Chat Composer: Emoji, Camera, and Voice Messages

This update enables full functionality for the chat composer, allowing users to send emojis, capture and send photos directly from the camera, and record/send voice messages.

## Features

### 1. Emoji Support
-   A dedicated emoji button opens a set of common emojis.
-   Tapping an emoji appends it to the current message text.
-   Supports sending emoji-only messages.

### 2. Camera Capture
-   The camera button requests `CAMERA` permission on demand.
-   Uses `ActivityResultContracts.TakePicture` with a secure `FileProvider` URI.
-   Captured photos are uploaded to Cloudinary as image messages.

### 3. Voice Messages
-   The microphone button requests `RECORD_AUDIO` permission.
-   Uses `MediaRecorder` to capture audio in `.m4a` (AAC) format.
-   **UI:** Shows a recording timer and provides "Cancel" and "Send" actions.
-   **Playback:** Tap a voice message bubble to play/pause using `MediaPlayer`.
-   **Storage:** Uploaded as `raw` resource type to Cloudinary.

## Permissions & Manifest
-   `android.permission.CAMERA`
-   `android.permission.RECORD_AUDIO`
-   `androidx.core.content.FileProvider` configured for secure URI generation.

## Firestore Structure
Voice messages use `type: "voice"` and include:
-   `mediaUrl`: Cloudinary secure URL.
-   `fileSizeBytes`: For metadata rendering.
-   `durationMillis`: Recording length.

## Manual Test Flow

### Emulator / Real Phone
1.  **Emoji:** Tap the emoji icon, select 😀, and send.
2.  **Camera:** Tap the camera icon, grant permission, take a photo, and send.
3.  **Voice:**
    -   Tap the microphone icon (ensure text input is empty).
    -   Grant permission.
    -   Record for 5 seconds (observe timer).
    -   Tap Send (right icon).
    -   Tap the resulting bubble to play back.
    -   Try another recording and tap "Cancel" to verify deletion.
