# Step 89: Firestore Call Session and State Verification

## Verified Model

`CallSession` maps `callId`, caller/receiver IDs, audio/video type, all approved statuses, offer, answer, and created/accepted/ended timestamps. Firestore documents use the same field names except `callId`, which is the document ID.

## Repository Contract

- `createCallOffer` creates the ringing session and SDP offer atomically.
- `listenForIncomingCalls` listens to receiver-owned calls and exposes ringing sessions.
- `listenToCall` observes one participant-authorized call.
- `acceptCallWithAnswer` writes answer, accepted status, and server timestamp atomically.
- `rejectCall` and `endCall` write terminal status and server timestamp together.
- Caller/receiver ICE add and listener methods use their explicit subcollections.

## Paths

```text
calls/{callId}
calls/{callId}/callerCandidates/{candidateId}
calls/{callId}/receiverCandidates/{candidateId}
```

## Rules

Only authenticated caller/receiver users can read a call. Creation requires the authenticated caller, an existing unblocked receiver, a valid type, a ringing state, a bounded non-empty offer, and server timestamps. Only the receiver can atomically accept with an answer or reject/busy; only the caller can cancel; either participant can end an accepted call. ICE candidates are append-only and role-owned.

## Verification Boundary

`git diff --check`, required API/path search, and targeted rule review were used. Gradle build, tests, emulator, WebRTC media, call UI, notifications, and Step 90 work were intentionally excluded.
