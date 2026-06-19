# Step 83: Message Composer State Hardening

## Goal

Keep reply, edit, attachment upload, and message actions from entering conflicting states.

## Implementation

- Starting a reply clears an active edit and its draft.
- Starting an edit clears reply mode and resets stale typing state.
- Reply/edit actions are ignored while a send or upload is in progress.
- Message long-press actions do not open while sending.
- Photo and document attachment actions are disabled during text editing.
- Repository entry points also reject media sends during edit mode.
- Deleting the message currently being edited clears its composer draft.

## Verification

Low-token mode remains active, so Gradle build/test was not run. The change was checked with targeted source review and `git diff --check`.

## Commit

`fix(chat): harden composer action states`
