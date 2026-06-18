# Step 71: Active Chat Notification Suppression

## Goal

Avoid showing a redundant notification when the user is already viewing the incoming message's conversation.

## Implementation

- Tracks process foreground/background state with `ProcessLifecycleOwner`.
- Tracks the conversation currently composed on screen.
- Suppresses only when the app is foregrounded and the payload targets that exact conversation.
- Background and other-conversation notifications continue normally.
- Added three local unit tests for the visibility policy.

## Verification

Run `./gradlew testDebugUnitTest assembleDebug`.

Manually send messages to the open conversation, another conversation, and while the app is backgrounded.

## Commit

`fix(notifications): suppress active chat alerts`
