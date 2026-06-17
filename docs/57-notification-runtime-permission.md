# Step 57 - Notification Runtime Permission

## Goal

Request Android 13+ notification permission so ContactMe can show push notifications when notification display is added.

## What Changed

- Added an Activity Result permission launcher in `MainActivity`.
- Checks `POST_NOTIFICATIONS` only on Android 13+.
- Requests the permission when it is not already granted.
- Keeps the app usable even if the user denies permission.

## Verification

1. Run `./gradlew.bat assembleDebug`.
2. Install/run on Android 13+ emulator or device.
3. Confirm the system notification permission dialog appears.
4. Deny permission and confirm the app still opens.
5. Allow permission and confirm the app still opens.

## Scope

This step only requests permission. It does not yet render message notifications or deep-link from notifications.
