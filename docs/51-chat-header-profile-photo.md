# Step 51 - Chat Header Profile Photo

## Goal

Show the peer profile photo in the chat detail header when a conversation is opened from recent chats, saved contacts, or discovery.

## What Changed

- Extended `ChatTarget` with `photoUrl`.
- Updated Home conversation callbacks to pass `photoUrl`.
- Updated direct conversation open flow to return the selected user's `photoUrl`.
- Added `chatPhotoUrl` to `ChatDetailScreen`.
- Updated `ChatHeaderTitle` to render `AsyncImage` when `chatPhotoUrl` exists and initials otherwise.

## Verification

1. Upload a profile photo for a test user.
2. Search/open that user from discovery or saved contacts.
3. Confirm the chat detail header shows the user's photo.
4. Return to recent chats and open the same conversation.
5. Confirm the header still shows the same photo.
6. Run `./gradlew.bat assembleDebug`.
