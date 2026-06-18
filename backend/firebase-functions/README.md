# ContactMe Firebase Functions

Trusted server-side functions for message notification fanout and later call/moderation workflows.

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
