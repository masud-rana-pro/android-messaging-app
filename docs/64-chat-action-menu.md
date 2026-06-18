# Step 64 - Chat Action Menu

## Goal

Move chat safety actions into a compact overflow menu so the chat header stays clean.

## What Changed

- Replaced visible `Report` and `Block` header text buttons with a `⋮` menu trigger.
- Added `ChatActionMenu`.
- Menu contains:
  - `Report`
  - `Block` or `Unblock` depending on chat safety state
- Kept existing safety behavior unchanged.

## Verification

1. Run `./gradlew.bat assembleDebug`.
2. Open a real chat.
3. Tap `⋮`.
4. Confirm `Report` and `Block` are shown.
5. Block the chat and reopen the menu.
6. Confirm `Unblock` is shown.

## Scope

This is UI polish only. Report reason selection and a richer bottom sheet can come later.
