# Step 90: Outgoing One-to-One Audio Call

Implemented the caller-side outgoing audio call flow using Pure WebRTC and Firestore signaling.

## Key Components

### 1. WebRtcCallEngine
A wrapper around PeerConnection that handles:
- PeerConnection initialization with audio-only tracks.
- SDP Offer generation and setting local description.
- Handling ICE candidates and connection state changes.
- Setting remote answer as remote description.
- Adding received ICE candidates to the connection.

### 2. OutgoingCallViewModel
Orchestrates the calling process:
- Initiates the call session in Firestore using `CallSignalingRepository`.
- Initializes `WebRtcCallEngine` and creates an offer.
- Listens for signaling updates (Answer/Status) from Firestore.
- Handles ICE candidate exchange between caller and receiver.
- Manages call states: `Ringing`, `Accepted`, `Rejected`, `Ended`, etc.

### 3. OutgoingCallScreen
A minimal Compose UI showing:
- Receiver ID (placeholder for now).
- Call status and connection state.
- A "Cancel" button to end the call attempt.

### 4. Navigation Integration
- Added `OutgoingCall` screen to `AppScreen` and `ContactMeApp` navigation.
- Added a "Call" button in `ChatDetailScreen` for direct conversations.
- Peer user ID is extracted from the direct conversation ID (`userId1__userId2`).

## Verification
- Code successfully compiled with WebRTC dependencies.
- Navigation flow from Chat -> Outgoing Call screen verified.
- Signaling document creation in Firestore verified via repository logic.
