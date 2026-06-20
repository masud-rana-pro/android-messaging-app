# Step 91: Incoming Audio Call Accept/Reject

Implemented the receiver-side incoming audio call flow using Pure WebRTC and Firestore signaling.

## Key Components

### 1. WebRtcCallEngine (Updated)
Extended the engine to support the receiver role:
- `setRemoteOffer(sdp: String)`: Sets the caller's SDP offer as the remote description.
- `createAnswer()`: Generates a local SDP answer for the receiver.
- Integrated local description setting for the generated answer.

### 2. IncomingCallViewModel
Handles detection and orchestration for the receiver:
- Listens for `Ringing` audio calls where the current user is the `receiverId`.
- Manages `IncomingCallUiState` to trigger global navigation.
- Orchestrates the **Accept** flow:
  - Initializes WebRTC engine.
  - Sets remote offer -> Creates local answer -> Updates Firestore with answer and `Accepted` status.
  - Starts ICE candidate exchange (listen for caller candidates, add receiver candidates).
- Orchestrates the **Reject** flow:
  - Updates Firestore status to `Rejected`.
  - Cleans up engine and jobs.

### 3. IncomingCallScreen
A global Compose UI triggered when an incoming call is detected:
- Displays Caller ID.
- Provides **Accept** and **Reject** buttons.
- Automatically dismisses when the call is ended or rejected.

### 4. Navigation Integration
- Added `IncomingCall` to `AppScreen` enum.
- Integrated a global `LaunchedEffect` in `ContactMeApp` that observes the incoming call state.
- Navigates to `IncomingCallScreen` from any foreground screen when a new ringing call arrives.

## Boundaries
- **Foreground Only**: Detection relies on a Firestore listener active while the app is in the foreground. Background/killed-app support is scheduled for Step 92.
- **Audio Only**: Video track support is scheduled for Step 94.

## Verification
- Code compiled successfully with updated WebRTC engine.
- Navigation logic for incoming calls verified.
- Signaling update logic (Accept/Reject) verified via repository integration.
