# Notification Flow

Notifications will be added after Chat MVP polish, so payloads and deep links are based on stable conversation and message models.

## Planned Components

- Firebase Cloud Messaging.
- `FirebaseMessagingService`.
- Notification channels.
- Cloud Functions for secure fanout.
- Device token storage.
- Deep link navigation into chat/call screens.

## Device Token Flow

1. User signs in.
2. Android receives an FCM token.
3. App stores token under:

```text
user_devices/{uid}/devices/{deviceId}
```

4. Token is refreshed when FCM rotates it.
5. Sign-out should stop using that local device token for the old user.

## Message Notification Flow

1. Sender creates a message in Firestore.
2. Cloud Function validates the conversation and participants.
3. Cloud Function loads receiver device tokens.
4. FCM sends a message notification.
5. Android shows notification in the `messages` channel.
6. Tapping notification opens the exact conversation.

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
