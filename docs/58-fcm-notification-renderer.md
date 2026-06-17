# Step 58 - FCM Notification Renderer

## Goal

Render incoming foreground FCM messages as Android notifications using the notification channels created in the previous step.

## What Changed

- Added `ContactMeNotificationRenderer`.
- `ContactMeMessagingService.onMessageReceived(...)` now forwards FCM messages to the renderer.
- Renderer supports data payload fields:

```text
type
conversationId
title
body
```

- Chooses notification channel by `type`:
  - `call` -> calls
  - `system` -> system
  - default -> messages
- Checks `POST_NOTIFICATIONS` permission before rendering on Android 13+.
- Opens `MainActivity` when a notification is tapped.

## Example Data Payload

```json
{
  "type": "message",
  "conversationId": "direct_uidA_uidB",
  "title": "Masud Rana",
  "body": "New message"
}
```

## Verification

1. Run `./gradlew.bat assembleDebug`.
2. Keep the app installed and notification permission allowed.
3. Send a test FCM data payload to the device token.
4. Confirm a notification appears.
5. Tap the notification and confirm the app opens.

## Scope

This step does not yet add deep-link navigation to a specific conversation. That will require route/deep-link state in the app shell.
