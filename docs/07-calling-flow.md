# Calling Flow

Voice/video calling will use ZegoCloud for the media session. Firebase stores call state, notifications, and history.

## Planned Components

- ZegoCloud SDK.
- Firestore `calls/{callId}`.
- Realtime Database `call_ringing/{receiverUid}/{callId}`.
- FCM for incoming and missed call notifications.
- Foreground service for active call.
- Notification actions for accept/reject if needed.

## One-to-One Call Flow

1. Caller starts voice or video call.
2. App creates a `calls/{callId}` document.
3. App writes ringing state for the receiver.
4. Cloud Function sends incoming call notification.
5. Receiver sees incoming call screen.
6. Receiver accepts or rejects.
7. ZegoCloud handles the media session.
8. App stores call history after ended, missed, rejected, or failed state.

## Planned Call Model

```text
calls/{callId}
  callerId
  receiverId
  type: "voice" | "video"
  status: "ringing" | "accepted" | "rejected" | "missed" | "ended" | "failed"
  startedAt
  acceptedAt
  endedAt
  durationSeconds
```

## Android Permissions

- Voice call: `RECORD_AUDIO`.
- Video call: `CAMERA`, `RECORD_AUDIO`.
- Incoming/ongoing call notification: notification permission where required.

## Rules

- Do not add foreground service before calling starts.
- Do not store Zego secrets in the Android client.
- Do not start group calling before one-to-one calling is stable.
