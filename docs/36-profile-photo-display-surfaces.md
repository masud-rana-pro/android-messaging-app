# Profile Photo Display Surfaces

## Goal

Use saved profile photo URLs in the main people-facing surfaces.

## What Changed

- `ConversationPreview` now includes `photoUrl`.
- Conversation preview loading now reads the other user's `photoUrl`.
- Search result avatars now show profile photos when available.
- Conversation list avatars now show profile photos when available.
- Added a reusable `ContactAvatar` composable with initials fallback.

## Current Scope

This step displays profile photos in:

- contact search results
- conversation list rows

It does not yet display profile photos in:

- chat detail top bar
- settings profile header
- group/member surfaces

## Verification

1. Upload a profile photo from profile setup/edit.
2. Search that user from another account.
3. Confirm the search result shows the uploaded photo.
4. Open a conversation with that user.
5. Confirm the conversation list row shows the uploaded photo.
6. Run `assembleDebug`.
