# Architecture

ContactMe is an Android-first app. There is no separate web app in the current build plan, but the app still needs backend services for auth, realtime chat, media storage, notifications, calling state, and moderation. The backend strategy is Firebase-first.

## Current Android Stack

- Kotlin
- Jetpack Compose
- Material 3
- Hilt
- Coroutines
- Flow
- Firebase Auth
- Firestore
- Cloudinary unsigned upload for current media/profile photo uploads

The Android app lives in:

```text
apps/ContactMe
```

## Current Backend Stack

| Area | Service |
| --- | --- |
| Auth | Firebase Auth |
| Realtime chat | Firestore listeners |
| Presence and call ringing | Firebase Realtime Database |
| Media | Cloudinary unsigned upload for MVP, with Firestore metadata |
| Push notifications | FCM |
| Server-side notification fanout | Cloud Functions |
| Security | Firestore and Realtime Database rules; Cloudinary preset restrictions |

Firebase Storage is not part of the active free-plan media path because the current Firebase project cannot enable Storage without billing. If billing is added later, media can move behind a signed backend upload flow.

## Planned Android Components

| Component | Add When Needed For |
| --- | --- |
| Main Activity | Current Compose host. |
| FileProvider | Media sharing/attachments. |
| FirebaseMessagingService | FCM token and foreground push handling. |
| WorkManager | Media upload retry, cleanup, token sync, backup/offline tasks. |
| Foreground Service | Active voice/video calls. |
| BroadcastReceiver | Notification call actions only if needed. |
| Room | Later offline/cache phase, after message schema stabilizes. |

## Architecture Rules

- Keep Firebase logic inside repositories, not composable screens.
- Keep `MainActivity` thin.
- Use ViewModels for screen state and user actions.
- Use repository interfaces so fake and Firebase implementations can coexist.
- Add Android system components only when a feature needs them.
- Do not hardcode FCM server keys, Zego secrets, or production secrets in the Android client.

## Scale Path

Firebase is the MVP backend. If the product grows beyond Firebase limits, the upgrade path can add:

- Custom API server for business logic.
- Dedicated realtime server for high-scale chat.
- Dedicated notification service.
- Search service such as Meilisearch, Elastic, or OpenSearch.
- Admin dashboard for moderation and support.
