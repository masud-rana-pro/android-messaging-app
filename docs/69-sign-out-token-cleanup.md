# Step 69: Sign-Out Token Cleanup

## Goal

Stop a signed-out device from receiving notifications for the previous account.

## Implementation

- `DeviceTokenRepository` now exposes current-device removal.
- The Firebase implementation deletes the device document and invalidates the local FCM token.
- `SessionViewModel` performs cleanup before Firebase Auth sign-out.
- The token-sync cache resets after sign-out so a later login registers a fresh token.
- Sign-out still completes if cleanup encounters a network error.

## Verification

1. Sign in and verify the current device exists under `user_devices/{uid}/devices`.
2. Sign out and verify that device document is removed.
3. Sign in again and verify a device document with a fresh token is created.

## Commit

`fix(notifications): remove device token on sign out`
