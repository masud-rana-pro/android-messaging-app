# Step 84: Background Message Notification Delivery

## Goal

Make server fanout use the Android app's existing notification renderer and conversation deep-link path in both foreground and background states.

## Implementation

- Kept the existing trusted Firestore message-created Cloud Function.
- Changed FCM fanout from mixed notification/data payloads to high-priority data-only payloads.
- Added a 24-hour Android message TTL.
- The Android `FirebaseMessagingService` now remains the single notification rendering path.
- Existing payload fields continue to include conversation id/type, message id, title, body, and photo URL.
- Documented the Firebase project id needed when no `.firebaserc` alias exists.

## Why Data-Only

With a mixed payload, Android can render background notifications without calling the app renderer. That system-generated pending intent contains raw FCM keys rather than ContactMe's custom navigation extras. A high-priority data-only message reaches the messaging service, which creates the established deep-link pending intent consistently.

## Deployment Status

Deployment was not attempted because Firebase CLI is not installed in the current environment. Firebase CLI authentication and a Cloud Functions-supported billing plan are still required.

## Verification

Low-token mode remains active, so function build/test was not run. Targeted payload-contract review and `git diff --check` were used.

## Commit

`fix(notifications): unify background message delivery`
