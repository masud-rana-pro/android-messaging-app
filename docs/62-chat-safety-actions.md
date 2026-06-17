# Step 62 - Chat Safety Actions

## Goal

Expose basic block and report actions in the chat detail screen using the safety data foundation from the previous step.

## What Changed

- Added chat header actions:
  - `Report`
  - `Block`
- Added safety action state to `ChatDetailUiState`.
- Added `ChatDetailViewModel.blockCurrentChat()`.
- Added `ChatDetailViewModel.reportCurrentChat()`.
- Added conversation-peer safety helpers to `SafetyRepository`.
- Blocking a chat disables the input field and stops typing state.
- Reporting creates a real `reports` document with reason `other`.

## Verification

1. Run `./gradlew.bat assembleDebug`.
2. Open a real chat.
3. Tap `Report`.
4. Confirm a `reports` document is created.
5. Tap `Block`.
6. Confirm a `blocked_users/{uid}/items/{peerUid}` document is created.
7. Confirm the chat input becomes unavailable.

## Scope

This is a minimal first UI. Later polish can replace text actions with a menu and add reason selection, unblock UI, and blocked-user management.
