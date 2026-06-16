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

The Android app lives in:

```text
apps/ContactMe
```

## Planned Firebase Stack

| Area | Service |
| --- | --- |
| Auth | Firebase Auth |
| Realtime chat | Firestore listeners |
| Presence and call ringing | Firebase Realtime Database |
| Media | Firebase Storage |
| Push notifications | FCM |
| Server-side notification fanout | Cloud Functions |
| Security | Firestore, Storage, and Realtime Database rules |

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
