# Step 82: Free WebRTC Signaling Foundation

## Goal

Create the one-to-one audio/video call signaling boundary using Firebase Auth and Firestore only.

## Implementation

- Added audio/video call types and the required call statuses.
- Added a Firestore call session model with caller, receiver, offer, answer, and timestamps.
- Added repository operations for atomically creating a call with its offer, atomically accepting with an answer, observing incoming/current calls, terminal status updates, and role-specific ICE candidates.
- Added caller and receiver candidate subcollections.
- Added participant-only Firestore rules, immutable call identity, status transition ownership, SDP limits, and append-only ICE rules.
- No media transport occurs through Firestore; WebRTC carries audio/video peer-to-peer or through TURN.

## Collections

```text
calls/{callId}
calls/{callId}/callerCandidates/{candidateId}
calls/{callId}/receiverCandidates/{candidateId}
```

## Verification

Targeted config/source review and `git diff --check` only. Gradle build, tests, emulator, and two-phone verification were intentionally skipped under the limit-saving instruction.
