# Typing Indicator Foundation

## Goal

Add the first realtime typing indicator for direct conversations.

## What Changed

- Added `TypingRepository`.
- Added `FirebaseTypingRepository`.
- Added `FakeTypingRepository`.
- Added Hilt binding in `TypingModule`.
- Added `isOtherUserTyping` to `ChatDetailUiState`.
- `ChatDetailViewModel` now:
  - sets typing true when the current user writes message text
  - sets typing false when text becomes blank
  - sets typing false after successful send
  - observes whether another participant is typing
- `ChatDetailScreen` now shows `typing...` in the chat header.
- Firestore rules now allow participant-only typing reads and own typing writes.

## Firestore Shape

```text
conversations/{conversationId}/typing/{uid}
  userId
  isTyping
  updatedAt
```

## Security Rule

Only conversation participants can read typing state.

Only the signed-in user can create/update their own typing document.

## Current Scope

This is a foundation step. It does not yet add:

- typing debounce delay
- automatic timeout cleanup from the client
- Realtime Database fast typing path
- privacy control for typing visibility

The repository treats typing state older than 15 seconds as stale.

## Verification

1. Use two signed-in accounts in the same direct conversation.
2. Type text from account A.
3. Confirm account B sees `typing...` in the chat header.
4. Clear the message text or send it.
5. Confirm typing state clears.
6. Confirm Firestore has `conversations/{conversationId}/typing/{uid}`.
7. Deploy updated Firestore rules before testing on a project with strict rules.
8. Run `assembleDebug`.
