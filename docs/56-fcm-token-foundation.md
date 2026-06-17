# Step 56 - FCM Token Foundation

## Goal

Add the first real notification foundation: FCM dependency, Android notification channels, Firebase Messaging service, and Firestore device-token sync.

## What Changed

- Added Firebase Messaging dependency.
- Added `POST_NOTIFICATIONS` permission.
- Registered `ContactMeMessagingService`.
- Created notification channels on app startup:
  - `messages`
  - `calls`
  - `system`
- Added `DeviceTokenRepository`.
- Added `FirebaseDeviceTokenRepository`.
- Added `DeviceTokenSyncViewModel`.
- Synced the current Android FCM token to:

```text
user_devices/{uid}/devices/{androidId}
```

- Added Firestore rules so users can manage only their own device token documents.

## Firestore Shape

```text
user_devices/{uid}/devices/{deviceId}
  token
  platform: "android"
  updatedAt
```

## Verification

1. Run `./gradlew.bat assembleDebug`.
2. Launch the app with a signed-in user.
3. Confirm Firestore has `user_devices/{uid}/devices/{deviceId}`.
4. Confirm the document contains `token`, `platform`, and `updatedAt`.
5. On Android 13+, notification runtime permission UI is still a later step.

## Scope

This step does not send push notifications yet. Cloud Functions fanout and notification UI display come after the token foundation is reliable.
