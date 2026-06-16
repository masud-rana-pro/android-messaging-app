# Message Status Foundation

## Goal

This step adds the first message status layer for chat messages.

## What Changed

- Added `MessageStatus`.
- Added `status` to `ChatMessage`.
- Saved new outgoing text messages with `status = "sent"`.
- Read message status from Firestore when messages are observed.
- Show a small sent mark beside the timestamp for messages sent by the current user.

## Current Scope

Only `Sent` is implemented in this step.

Delivered and read receipts need more data, such as recipient device delivery state or recipient read timestamps. Those should not be faked in the UI.

## Verification

1. Send a message from the chat detail screen.
2. Confirm the message appears in the chat.
3. Confirm the sender's bubble shows a small check mark beside the time.
4. Confirm the app builds successfully with `assembleDebug`.
