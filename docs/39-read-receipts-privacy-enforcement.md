# Read Receipts Privacy Enforcement

## Goal

Use the saved `readReceiptsEnabled` privacy setting when showing message read status.

## What Changed

- Added `ReadReceiptState`.
- `ConversationRepository` can now observe peer read receipt state.
- `FirebaseConversationRepository` now observes:
  - peer `readAtByUser.{peerUid}` from the conversation document
  - peer `users/{peerUid}.readReceiptsEnabled`
- `ChatDetailUiState` now includes `readReceiptState`.
- `ChatDetailViewModel` observes read receipt state for the active conversation.
- Own message status now shows:
  - `Seen` only if peer read timestamp covers the message and peer allows read receipts
  - otherwise `Sent`

## Current Scope

This step enforces read receipt visibility in the UI.

It does not yet hide the underlying conversation read timestamp from Firestore participants. A later schema/rules hardening step can split local unread clearing from public read receipts.

## Verification

1. Use two accounts in one chat.
2. User B enables read receipts.
3. User A sends a message.
4. User B opens the chat.
5. Confirm User A can see `Seen`.
6. User B disables read receipts.
7. User A sends another message.
8. User B opens the chat.
9. Confirm User A still sees `Sent`, not `Seen`.
10. Run `assembleDebug`.
