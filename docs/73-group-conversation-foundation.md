# Step 73: Group Conversation Foundation

## Goal

Add the real group conversation model, creation contract, preview mapping, validation, and Firestore authorization required before group UI.

## Implementation

- Added direct/group conversation types.
- Added group creation with owner, admins, participants, title, timestamps, and generated ID.
- Requires two contacts besides the creator and caps groups at 256 participants.
- Conversation list maps group title/photo directly instead of treating a group as a direct peer.
- Firestore rules validate group creation and restrict metadata updates to admins.
- Added local group validation tests.

## Verification

Run Android tests/build and deploy Firestore rules before real group creation testing.

## Commit

`feat(groups): add group conversation foundation`
