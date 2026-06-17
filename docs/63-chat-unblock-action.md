# Step 63 - Chat Unblock Action

## Goal

Complete the first block UX loop by detecting blocked chats and allowing the current user to unblock a peer from the chat screen.

## What Changed

- Added safety repository helpers:
  - `unblockConversationPeer`
  - `hasCurrentUserBlockedConversationPeer`
  - `hasBlockInConversation`
- Chat detail now loads safety state when a conversation opens.
- If any block exists, the chat input is disabled.
- If the current user created the block, the header action changes from `Block` to `Unblock`.
- Unblocking removes the current user's block document and re-enables the chat input.

## Verification

1. Open a real chat.
2. Tap `Block`.
3. Confirm input shows unavailable state.
4. Confirm header action changes to `Unblock`.
5. Tap `Unblock`.
6. Confirm input is enabled again.
7. Run `./gradlew.bat assembleDebug`.

## Scope

If the other user blocked the current user, the app keeps the chat unavailable and does not show an unblock path, because the current user cannot remove someone else's block.
