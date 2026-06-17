# Step 60 - Notification Chat Deep Link

## Goal

Open a specific chat when a user taps an FCM notification that contains a conversation id.

## What Changed

- Added `NotificationNavigation`.
- Notification pending intents now include:

```text
conversationId
chat title
chat photo URL
```

- `MainActivity` reads notification extras on cold start.
- `MainActivity.onNewIntent(...)` handles notification taps while the app is already running.
- `ContactMeApp` opens the target chat after the app is in an authenticated app screen.

## Expected FCM Data Payload

```json
{
  "type": "message",
  "conversationId": "direct_uidA_uidB",
  "title": "Masud Rana",
  "photoUrl": "https://res.cloudinary.com/...",
  "body": "New message"
}
```

## Verification

1. Run `./gradlew.bat assembleDebug`.
2. Send a test FCM data payload with `conversationId` and `title`.
3. Tap the notification.
4. Confirm the app opens the matching chat.
5. If the user is signed out, confirm the app still resolves the normal auth flow first.

## Scope

This is local app navigation foundation. It does not yet implement Cloud Functions fanout or notification grouping.
