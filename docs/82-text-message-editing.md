# Step 82: Sender Text Message Editing

## Goal

Allow senders to correct their own live text messages without replacing the message document.

## Implementation

- A sender-owned text message now shows **Edit** in its long-press action dialog.
- Edit mode reuses the composer and can be cancelled explicitly.
- A Firestore transaction verifies sender ownership, text type, and live-message state.
- The message stores `editedAt`; the bubble displays an `edited` marker.
- Editing the latest message also refreshes the conversation-list preview.
- Firestore rules allow only `text` and `editedAt` to change during an edit.
- Message transaction reads now happen before writes, including the existing deletion path.

## Verification

Build/test was intentionally deferred in low-token mode. `git diff --check` and targeted source review were used. Updated Firestore rules must be deployed before manual testing.

## Commit

`feat(chat): add text message editing`
