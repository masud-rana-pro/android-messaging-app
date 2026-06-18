# Step 68: Message Notification Fanout

## Goal

Send real FCM notifications from a trusted Firebase backend whenever a new chat message is created.

## Implementation

- Added a Node 20 TypeScript Firebase Functions project.
- Added a Firestore `onDocumentCreated` trigger for conversation messages.
- Resolves recipients from conversation participants and excludes the sender.
- Suppresses notifications when either participant has blocked the other.
- Loads every registered Android device token for each recipient.
- Sends FCM in batches of 500 and removes invalid tokens.
- Sends only safe preview/navigation data required by the Android app.

## Verification

Run `npm install` and `npm run build` in `backend/firebase-functions`.

After deployment, send a message while the recipient app is backgrounded and verify the notification opens the correct conversation.

## Deployment Requirement

Firebase CLI login and a project plan supporting Cloud Functions are required for deployment.

## Commit

`feat(notifications): add message fanout function`
