# Step 86: Message Sync and Send Reliability

## Goal

Expose message-stream failures clearly and prevent rapid repeated actions from starting duplicate sends.

## Implementation

- Firestore listener errors now close the message stream instead of emitting a false empty list.
- Chat state distinguishes a sync error from an empty conversation.
- The chat list renders a retry action that starts a fresh listener subscription.
- A successful snapshot clears the sync error and preserves normal read-marker behavior.
- Text send/edit, delete, image queue, and document queue lock `isSending` before launching their coroutine.
- Rapid repeated taps therefore see the busy state synchronously and are ignored.

## Verification

Low-token mode remains active, so Gradle build/test was not run. Targeted source-flow review and `git diff --check` were used.

## Commit

`fix(chat): harden message sync and send gating`
