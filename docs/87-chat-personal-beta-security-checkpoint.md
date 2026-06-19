# Step 87: Chat Personal-Beta Security Checkpoint

## Goal

Close the core chat implementation phase with secure read-receipt ownership and an explicit deployment/physical-device boundary.

## Implementation

- Firestore conversation updates now separate message-preview changes from read-receipt changes.
- A participant may change only their own key inside `readAtByUser`.
- The receipt value must resolve to Firestore `request.time`.
- Removing or changing another participant's receipt key is rejected.
- Roadmap state now reflects privacy-aware read receipts and the completed personal-beta chat code path.

## Personal-Beta Chat Scope Now Present

- Direct and group text messages.
- Image and document messages with background WorkManager delivery/retry.
- Reply, sender edit, and sender soft-delete.
- Conversation unread state and direct-chat seen markers.
- Typing, presence, block/report enforcement.
- FCM token sync, server fanout code, background rendering, and one-shot chat deep links.
- Explicit message-sync error and retry state.

## External Verification Still Required

This checkpoint does not claim a physical-device pass. Firebase rules and `sendMessageNotification` must be deployed, then two authenticated Android phones must run the documented message matrix. Firebase CLI is not installed in the current environment, and low-token mode disallows the local Gradle/function build step.

## Commit

`fix(chat): secure read receipt ownership`
