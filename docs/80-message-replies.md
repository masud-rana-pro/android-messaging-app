# Step 80: Message Replies

## Goal

Reply to an existing text, photo, or document while preserving clear message context.

## Implementation

- Long-pressing a message selects it as the reply target.
- The composer shows the target sender and a compact type-aware preview.
- Reply selection can be cancelled before sending.
- Text messages persist reply target ID, sender label, preview, and message type.
- Received reply bubbles render the quoted context above their content.
- Reply state clears only after a successful send.

## Verification

Manual verification is deferred to conserve the current Codex/build budget. Test replies to text, image, and document messages on two accounts before release.

## Commit

`feat(chat): add message replies`
