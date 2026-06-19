# ContactMe Firebase Functions

Trusted server-side functions for message notification fanout and later call/moderation workflows.

Message fanout uses a high-priority data-only FCM payload. The Android messaging service therefore
builds the same notification and conversation deep link in foreground and background states.

## Local build

```bash
npm install
npm run build
```

## Deploy

```bash
firebase deploy --only functions:sendMessageNotification
```

## ZEGOCLOUD call token configuration

`issueZegoCallToken` returns a short-lived room-scoped Token04 only to an authenticated participant
listed in `calls/{callId}`. Store the 32-byte ServerSecret in Firebase Secret Manager:

```bash
firebase functions:secrets:set ZEGO_SERVER_SECRET
firebase deploy --only functions:issueZegoCallToken --project messasing-app-9c367
```

The first deploy prompts for the non-secret `ZEGO_APP_ID` parameter. Never put ServerSecret or
AppSign in Android resources, Gradle properties, Firestore, Git, or an FCM payload.

Deployment requires Firebase CLI authentication and a project plan that supports Cloud Functions.
The current Android Firebase project id is `messasing-app-9c367`; pass it with `--project` when no
local `.firebaserc` alias is configured.
