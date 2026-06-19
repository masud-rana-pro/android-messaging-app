# Step 85: One-Shot Notification Navigation

## Goal

Consume a notification chat target once so normal back navigation cannot reopen the same chat unexpectedly.

## Implementation

- `ContactMeApp` reports when an authenticated screen has consumed the notification target.
- `MainActivity` clears the Compose state after consumption.
- The original intent's ContactMe navigation extras are removed as well.
- A target remains pending through splash/auth/profile setup and is consumed only when chat navigation is possible.
- New notification intents can still replace the current target through `onNewIntent(...)`.

## Fixed Behavior

Previously, returning from a notification-opened chat to Home changed the current screen and retriggered the same `LaunchedEffect`, reopening the chat. Activity recreation could also parse the old extras again. Both paths now use one-shot consumption.

## Verification

Low-token mode remains active, so Gradle build/test was not run. Targeted state-flow review and `git diff --check` were used.

## Commit

`fix(notifications): consume chat navigation targets`
