# Step 65: Report Reason Selection

## Goal

Allow a user to select a meaningful reason before reporting a direct-chat participant.

## Implementation

- The chat overflow menu opens a Material 3 report dialog.
- Available reasons are spam, harassment, scam or fraud, and other.
- `ChatDetailViewModel` now receives the selected `ReportReason` instead of always submitting `Other`.
- The existing safety repository persists the enum's stable Firestore value.

## Verification

1. Open a real direct conversation.
2. Open the overflow menu and select **Report**.
3. Select a reason.
4. Confirm that the success message appears.
5. In Firestore, verify the new report document contains the selected lowercase `reason` value.

## Commit

`feat(safety): add report reason selection`
