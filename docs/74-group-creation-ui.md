# Step 74: Group Creation UI

## Goal

Let users create a real group from saved contacts.

## Implementation

- Added a New group action to Chats.
- Added a dedicated group name and multi-contact selection screen.
- Added loading, empty, validation, progress, and error states.
- Connected the screen to the real group conversation repository.
- Returns to the live chat list after successful Firestore creation.
- Carries conversation type through chat navigation targets.

## Verification

Deploy current Firestore rules, save at least two contacts, create a group, and confirm it appears in Recent chats.

## Commit

`feat(groups): add group creation screen`
