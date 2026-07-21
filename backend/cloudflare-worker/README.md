# ContactMe Cloudflare Worker

This Worker sends ContactMe push notifications through FCM HTTP v1 without exposing privileged Firebase credentials in the Android app.

## Responsibilities

- Accept notification requests from the Android app backend-facing flow
- Verify Firestore call/message documents before sending anything
- Resolve receiver device tokens from `user_devices/{userId}/devices`
- Fall back to legacy `users/{userId}.fcmToken` when needed
- Send data-only FCM payloads for:
  - incoming one-to-one calls
  - direct messages
  - group messages
  - media, document, voice, and group call invitation messages

## Why This Exists

Android clients must not send FCM HTTP v1 requests directly because that would expose Firebase service-account credentials. The Worker keeps privileged credentials in Cloudflare secrets and performs server-side validation before sending notifications.

## Setup

Install dependencies:

```powershell
cd backend\cloudflare-worker
npm install
```

Set secrets:

```powershell
npx wrangler secret put FIREBASE_PROJECT_ID
npx wrangler secret put FIREBASE_CLIENT_EMAIL
npx wrangler secret put FIREBASE_PRIVATE_KEY
```

Deploy:

```powershell
npx wrangler deploy
```

## Local Files

```text
src/index.js       Worker implementation
wrangler.toml      Worker name, entry point, compatibility date
package.json       Wrangler dependency
```

## Request Payloads

Incoming call:

```json
{
  "callId": "firestore-call-id",
  "receiverId": "receiver-user-id"
}
```

Message:

```json
{
  "type": "message",
  "conversationId": "conversation-id",
  "messageId": "message-id",
  "senderId": "sender-user-id"
}
```

## Security Rules

The Worker uses Firebase service-account access to read only the documents it needs. Firestore client rules still protect Android clients. Keep these secrets out of Git:

- `FIREBASE_PRIVATE_KEY`
- `FIREBASE_CLIENT_EMAIL`
- `FIREBASE_PROJECT_ID`
- FCM access tokens
- TURN credentials
- Cloudinary credentials

## Android Integration

The Android app triggers this Worker after writing a valid message or call document. The received FCM data payload is rendered by:

```text
app/src/main/java/com/contactme/app/notification/
```

If push notifications are not arriving on a real phone, check these first:

- Worker is deployed after the latest `src/index.js` changes
- Worker secrets are set correctly
- The receiver has a document in `user_devices/{userId}/devices`
- Android notification permission is granted
- Firestore message/call document is fresh and valid
