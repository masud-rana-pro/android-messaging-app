# Product Roadmap

ContactMe is an Android-first WhatsApp-like messenger built in staged releases. The app keeps its own ContactMe identity and theme while using familiar messenger patterns for chats, media, calls, groups, status, channels, notifications, privacy, and release hardening.

The polished master roadmap is maintained in `ContactMe_Full_App_Roadmap.docx`. Markdown docs remain the implementation source of truth for day-to-day development.

## Current Checkpoint

The project has moved beyond the original UI demo. The current implemented foundation includes:

- Android Kotlin, Jetpack Compose, Hilt, Coroutines, and Flow scaffold.
- Firebase Auth setup with phone-first identity and optional email/password fallback.
- Session restore and logout.
- Firestore profile persistence.
- Username reservation and discovery.
- Direct one-to-one conversation creation.
- Real text message send and render.
- Real conversation list from Firestore.
- Chat detail timestamp and auto-scroll polish.
- Conversation list timestamp and unread/read foundation.
- Message sent status foundation.
- Firestore MVP security rules for users, usernames, direct conversations, and text messages.

## Roadmap Order

1. Current checkpoint and project truth.
2. Chat MVP finish.
3. Contacts, phone search, profile photo, and privacy.
4. Notifications with FCM and Cloud Functions.
5. Media messaging with Firebase Storage and WorkManager.
6. Groups and group admin.
7. Calls with ZegoCloud, FCM, and foreground service.
8. Status/stories.
9. Channels.
10. Security, moderation, reports, and block.
11. Offline/cache/backup.
12. Personal beta release hardening.

## Release Targets

| Release | Goal |
| --- | --- |
| v0.1 UI Demo | Basic screen map and visual direction. |
| v0.2 Auth/Profile | Firebase auth, session restore, profile persistence. |
| v0.3 Chat MVP | Direct conversations, realtime text messages, chat list. |
| v0.4 Chat Plus | Unread/read, status foundation, typing, presence, actions. |
| v0.5 Contacts/Privacy | Phone search, profile photo, block/report, privacy settings. |
| v0.6 Notifications | FCM token sync, notification channels, deep links. |
| v0.7 Media | Image/file messages, Storage, upload progress, retry. |
| v0.8 Groups | Group chat, members, roles, group settings. |
| v0.9 Calls | One-to-one voice/video calls with call history. |
| v1.0 Personal Beta | Stable APK for real-device testing with core messenger flows. |
| v1.5 Status/Channels | Status/stories and channel broadcast features. |
| v2.0 Production Candidate | Offline/cache, backup, moderation, performance, release hardening. |

## Product Defaults

- Platform: Android only for now.
- Backend: Firebase-first, not a separate web app.
- Identity: phone number primary, email/password optional.
- UI: WhatsApp-like custom ContactMe design, not a trademark clone.
- Security: practical secure MVP first; do not claim E2EE until it is actually designed, implemented, and tested.
- Learning: every implementation step must update `/learning` with detailed Bangla explanations.
