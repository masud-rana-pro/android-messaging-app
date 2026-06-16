# Chat Peer Presence Header

## Goal

Use the Realtime Database presence data from the previous step to show the other participant's online/last-seen state in the chat header.

## What Changed

- Added `PresenceStatus`.
- Extended `PresenceRepository` with `observeConversationPeerPresence`.
- `FirebasePresenceRepository` now:
  - listens to the conversation document
  - finds the other participant id
  - observes `presence/{peerUid}` in Realtime Database
  - emits online and last-seen state
- `ChatDetailUiState` now includes `peerPresence`.
- `ChatDetailViewModel` observes peer presence for real conversations.
- `ChatDetailScreen` subtitle now prioritizes:
  1. demo chat `online`
  2. `typing...`
  3. peer `online`
  4. formatted `last seen`
  5. fallback `last seen recently`

## Data Flow

```text
ChatDetailScreen
-> ChatDetailViewModel.openConversation(conversationId)
-> PresenceRepository.observeConversationPeerPresence()
-> Firestore conversations/{conversationId}
-> participantIds -> peer uid
-> Realtime Database presence/{peerUid}
-> ChatDetailUiState.peerPresence
-> header subtitle
```

## Current Scope

This is a direct conversation foundation. Group presence and privacy controls are not included yet.

## Verification

1. Use two accounts in a direct conversation.
2. Open the chat from account A.
3. Put account B in foreground.
4. Confirm account A sees `online`.
5. Background account B.
6. Confirm account A eventually sees `last seen h:mm AM/PM`.
7. Type from account B and confirm `typing...` overrides `online`.
8. Run `assembleDebug`.
