# Step 66: Block Confirmation And Roadmap Sync

## Goal

Prevent accidental user blocking and align the roadmap with the implemented safety foundation.

## Implementation

- Selecting **Block** now opens a Material 3 confirmation dialog.
- The repository action runs only after explicit confirmation.
- Cancel and outside-dismiss leave the chat state unchanged.
- The current-state and security documents now record the completed block/report foundation.

## Verification

1. Open a real direct conversation and select **Block** from the overflow menu.
2. Select **Cancel** and confirm messaging remains available.
3. Open the dialog again and select **Block**.
4. Confirm the composer becomes unavailable and the blocked-user document exists in Firestore.

## Commit

`feat(safety): confirm user blocking`
