# Step 76: Group Message Sender Labels

## Goal

Identify the sender of each incoming group message without trusting client-authored display-name fields.

## Implementation

- Resolves sender names from authenticated user profile documents.
- Caches names by sender ID inside the repository to reduce repeated reads.
- Adds resolved sender name to the local chat message model only.
- Shows a compact sender label above incoming group message content.
- Direct chats and the current user's own group messages remain unchanged.

## Verification

Open a group with multiple accounts and confirm each incoming text/image bubble shows the correct profile display name.

## Commit

`feat(groups): show group message senders`
