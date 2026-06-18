# Step 70: Notification Payload Hardening

## Goal

Reject malformed message notifications and apply Android-standard privacy, category, and grouping metadata.

## Implementation

- Extracted notification payload parsing into a testable model.
- Message notifications without a conversation ID are rejected.
- Notification IDs are deterministic and non-negative.
- Conversation notifications share a stable group key.
- Notifications use private lock-screen visibility and appropriate Android categories.
- Added unit tests for invalid message, valid message, and system payloads.

## Verification

Run `./gradlew testDebugUnitTest assembleDebug` and confirm all payload tests and the APK build pass.

## Commit

`test(notifications): harden notification payloads`
