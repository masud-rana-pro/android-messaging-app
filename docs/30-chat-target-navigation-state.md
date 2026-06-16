# Chat Target Navigation State

## Goal

Clean up chat navigation state by keeping chat title and conversation id together.

## What Changed

- Added `ChatTarget`.
- Replaced separate `selectedChatName` and `selectedConversationId` state with one `selectedChatTarget`.
- Added a local `openChat(target)` helper in `ContactMeApp`.
- Existing demo chat flow still supports `conversationId = null`.
- Real conversation and discovered-user flows now open chat using the same target object.

## Why This Matters

The chat detail screen needs both:

- a display title
- an optional real conversation id

Keeping these values in separate state variables can create mismatches. For example, a title from one chat and a conversation id from another chat could accidentally be combined during future refactors.

`ChatTarget` keeps the navigation payload together and prepares the app for:

- notification deep links
- chat search result navigation
- contact profile to chat navigation
- future Navigation Compose route migration

## Verification

1. Open a demo chat from placeholder chat list.
2. Confirm demo chat still opens with `conversationId = null`.
3. Open a real conversation from the real conversation list.
4. Confirm the correct title and conversation id are passed to `ChatDetailScreen`.
5. Open a discovered user and confirm direct conversation still opens.
6. Run `assembleDebug`.
