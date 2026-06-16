# Chat State and Retry Polish

## Goal

Improve the chat detail MVP experience by separating loading, empty, and send-error states.

## What Changed

- Added `isLoadingMessages` to `ChatDetailUiState`.
- Chat detail now enters a loading state when a real conversation opens.
- The loading state clears after the first message snapshot arrives.
- Empty conversations now show a clear empty-state message.
- Send failures now render inside an error container with a retry action when message text is still available.
- The message status marker now uses ASCII-safe `Sent` text instead of a check-mark symbol.

## Why This Matters

Realtime chat screens need clear state boundaries:

- Loading means the app is waiting for the first synced snapshot.
- Empty means the conversation exists but has no messages.
- Error means the user's send action failed and can be retried.

Without this separation, an empty conversation and a loading conversation look the same.

## Verification

1. Open a real conversation.
2. Confirm the chat shows a loading state before the first snapshot.
3. Confirm an empty conversation shows `No messages yet`.
4. Type a message and send it.
5. If sending fails, confirm the error box appears and the message text remains available for retry.
6. Confirm sent messages show `Sent` beside the timestamp.
7. Run `assembleDebug`.
