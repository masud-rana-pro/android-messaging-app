# Step 81: Sender Message Deletion

## Goal

Allow a sender to remove their own message content while preserving conversation chronology.

## Implementation

- Long-press opens a message action dialog.
- Reply is available for any live message; Delete is shown only for the sender's message.
- Firestore transaction verifies the sender before soft deletion.
- Text and media metadata are cleared and deletion timestamp/state are stored.
- Deleted bubbles render a standard deletion placeholder.
- New messages store `lastMessageId`; deleting the latest message updates the chat-list preview.
- Firestore rules enforce sender ownership and restrict changed fields.

## Verification

Build/test is deferred to conserve the current Codex budget. Updated Firestore rules must be deployed before manual deletion testing.

## Commit

`feat(chat): add sender message deletion`
