# Step 75: Group Chat Behavior

## Goal

Open group conversations without applying direct-peer safety, presence, typing, or read-receipt behavior to an arbitrary member.

## Implementation

- Passes conversation type from the list through navigation into chat.
- Group headers show a neutral Group subtitle.
- Direct-only block/report actions are hidden for groups.
- Direct-peer presence, typing, read receipts, and safety lookup are not started for groups.
- Message sending skips one-to-one block lookup for group documents.
- Existing text and background image message flows remain available.

## Verification

Create and open a group, send text and image messages, and confirm no direct-user block/report menu or last-seen status appears.

## Commit

`fix(groups): separate group chat behavior`
