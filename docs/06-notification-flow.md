# Notification Flow

Notification foundation has started. The app can create channels, sync device tokens, request Android 13+ notification permission, and render incoming foreground FCM payloads. Cloud Functions fanout and deep links are still planned.

## Current Components

- Firebase Cloud Messaging dependency.
- `ContactMeMessagingService`.
- Notification channels: `messages`, `calls`, `system`.
- Device token storage under `user_devices/{uid}/devices/{deviceId}`.
- Runtime `POST_NOTIFICATIONS` request.
- Foreground notification renderer.

## Planned Components

- Cloud Functions for secure fanout.
- Deep link navigation into chat/call screens.
- Notification action buttons for calls.

## Device Token Flow

1. User signs in.
2. Android receives an FCM token.
3. App stores token under:

```text
user_devices/{uid}/devices/{deviceId}
```

4. Token is refreshed when FCM rotates it.
5. Sign-out token cleanup is still planned.

## Message Notification Flow

1. Sender creates a message in Firestore.
2. Cloud Function validates the conversation and participants.
3. Cloud Function loads receiver device tokens.
4. FCM sends a message notification.
5. Android shows notification in the `messages` channel.
6. Tapping notification opens the exact conversation.

Current Android client opens the app from a notification tap. Exact conversation deep-linking is still planned.

## Call Notification Flow

1. Caller creates a call document and ringing state.
2. Cloud Function sends an incoming-call notification.
3. Android opens incoming call UI through notification/deep link.
4. Accept/reject actions update call state.

## Planned Notification Channels

| Channel | Purpose |
| --- | --- |
| messages | Direct chat messages. |
| groups | Group messages. |
| calls | Incoming, missed, and ongoing calls. |
| channels | Channel posts. |
| status | Status/story interactions if needed. |
| system | Account/session/system notices. |

## Rules

- The Android client must not send push notifications directly to other users.
- Notification payloads must not contain sensitive full message data beyond what is safe to show on lock screen.
- Android 13+ must request `POST_NOTIFICATIONS`.
