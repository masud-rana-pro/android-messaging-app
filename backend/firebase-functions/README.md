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

Deployment requires Firebase CLI authentication and a project plan that supports Cloud Functions.
The current Android Firebase project id is `messasing-app-9c367`; pass it with `--project` when no
local `.firebaserc` alias is configured.
