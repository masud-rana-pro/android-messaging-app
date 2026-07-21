# ContactMe

ContactMe is an Android messaging app focused on realtime chat, media sharing, presence, notifications, and audio/video calling. The app is built with Kotlin, Jetpack Compose, Firebase, WebRTC, and a Cloudflare Worker for server-side FCM fanout.

## What Works Now

- Email registration, login, logout, and password reset
- Profile setup with display name, username, phone number, and profile photo
- Direct conversations with realtime text messages
- Image, document, and voice message support
- Reply, edit, delete, report, block, and unblock chat actions
- Conversation list with unread state and call history cards in chat
- Online/last-seen presence backed by Firebase Realtime Database and Firestore
- One-to-one audio and video calling with WebRTC + TURN
- Incoming call foreground notifications and active call restoration
- Group creation from registered app users
- Group chat and group audio/video call invitations through Jitsi links
- Message push notifications through Cloudflare Worker + FCM
- Polished Compose UI theme, logo, chat wallpaper, settings, and call screens

## Repository Layout

```text
apps/ContactMe/              Android app
backend/cloudflare-worker/   FCM notification Worker
firebase/                    Firestore, Realtime Database, and Storage rules
docs/                        Roadmap, architecture, feature notes, checklists
learning/                    Step-by-step implementation notes
design/                      Design references and exported assets
```

## Android App

Open this folder in Android Studio:

```text
apps/ContactMe
```

Build and test from PowerShell:

```powershell
cd apps\ContactMe
.\gradlew.bat assembleDebug testDebugUnitTest
```

If Gradle wrapper download is blocked on the local machine, use the installed Gradle distribution under `%USERPROFILE%\.gradle\wrapper\dists\...`.

## Firebase Setup

The app expects Firebase Auth, Firestore, Realtime Database, Storage, and FCM to be configured for the Android package.

Deploy rules from the repo root:

```powershell
firebase deploy --only firestore:rules,database,storage --project <firebase-project-id>
```

Keep `google-services.json` local unless you intentionally decide to commit environment-specific Firebase config.

## Cloudflare Worker

The Worker verifies message/call requests, reads Firestore with a service account, and sends FCM HTTP v1 data messages. Source lives in:

```text
backend/cloudflare-worker
```

Before deploying, set Worker secrets:

```powershell
cd backend\cloudflare-worker
npx wrangler secret put FIREBASE_PROJECT_ID
npx wrangler secret put FIREBASE_CLIENT_EMAIL
npx wrangler secret put FIREBASE_PRIVATE_KEY
npx wrangler deploy
```

The Android app currently calls the deployed Worker URL from `FirebaseMessageRepository`.

## Calling Notes

One-to-one calls use native WebRTC signaling documents in Firestore. Real phones usually need a working TURN server, especially across mobile data and different Wi-Fi networks.

Do not commit real TURN, Firebase service-account, Cloudinary, signing, or API secrets. Prefer local files, Gradle properties injected outside Git, Firebase secrets, or Cloudflare Worker secrets.

## Useful Docs

- [Architecture](docs/03-architecture.md)
- [Database schema](docs/04-database-schema.md)
- [Security rules](docs/05-security-rules.md)
- [Notification flow](docs/06-notification-flow.md)
- [Calling flow](docs/07-calling-flow.md)
- [Release checklist](docs/08-release-checklist.md)
- [Current state roadmap](docs/28-current-state-and-next-roadmap.md)

## Current Local Notes

The latest verified command was:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest
```

It completed successfully after the Start Chat app-user search fix.
