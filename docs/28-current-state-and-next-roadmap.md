# Current State and Next Roadmap

This document is the implementation checkpoint after the message status foundation. It aligns the old full roadmap with the actual repository state.

## Project Truth

- App name: ContactMe.
- Platform: Android first.
- Android project path: `apps/ContactMe`.
- Backend strategy: Firebase-first for auth/data, Cloudinary for current media upload.
- Current branch: `feature/auth-build`.
- Current release direction: personal beta APK before Play Store hardening.
- UI direction: WhatsApp-like custom ContactMe theme, not a trademark clone.
- Learning rule: every implementation step must update `/learning` with detailed Bangla explanation.

## Completed Feature Matrix

| Area | Status | Notes |
| --- | --- | --- |
| Android scaffold | Done | Kotlin, Compose, Material 3, Hilt. |
| Firebase setup | Done | Auth and Firestore are connected. |
| Phone auth | Done foundation | Firebase Phone Auth OTP path exists. |
| Email fallback | Done foundation | Email/password login/register path exists. |
| Session restore/logout | Done | App can restore Firebase session and sign out. |
| Profile persistence | Done foundation | Profile data saves/loads from Firestore. |
| Username uniqueness | Done foundation | Username reservation and search exist. |
| Direct conversation | Done foundation | Direct conversation document can be created. |
| Text messages | Done foundation | Messages send/render from Firestore. |
| Conversation list | Done foundation | Firestore conversation previews exist. |
| Unread/read | Done foundation | Conversation-level read marker exists. |
| Message status | Done foundation | `sent` status exists; delivered/read not yet real. |
| Profile photo | Done MVP | Photo Picker uploads image to Cloudinary and stores `photoUrl` in Firestore. |
| Media messages | Done MVP | Chat images upload to Cloudinary and store metadata in Firestore. |
| Typing/presence | Done foundation | Firestore typing and Realtime Database presence foundations exist. |
| FCM token sync | Done foundation | Android token syncs to `user_devices/{uid}/devices/{deviceId}`. |
| Notification channels | Done foundation | Messages, calls, and system channels are created. |
| Notification permission | Done foundation | Android 13+ `POST_NOTIFICATIONS` runtime request exists. |
| Notification renderer | Done foundation | Foreground FCM payloads can render Android notifications. |
| Firestore rules | Done MVP | Rules cover current user/profile/direct chat/device-token flows. |

## Partial Or Not Started

| Area | Status | Next Need |
| --- | --- | --- |
| Chat UI polish | Partial | Better navigation, empty/loading/error states, retry. |
| Phone search | Done foundation | Normalize and query phone identity; native contacts sync remains later. |
| Privacy settings | Done foundation | Last seen/profile photo/read receipt settings exist; more hardening remains. |
| Block/report | Done foundation | Data model, rules, chat enforcement, reason selection, block/unblock UI; emulator tests and moderation tools remain. |
| Notifications | Partial | Cloud Functions fanout, deep links, and notification actions remain. |
| Media polish | Advanced foundation | Validation, compression, preview, retry, app-private pending files, and WorkManager background delivery exist; signed production upload remains. |
| Groups | Not started | Group model, members, roles, group messages. |
| Calls | Not started | ZegoCloud, call state, notification, foreground service. |
| Status/channels | Not started | Media/status/channel models and UI. |
| Offline/backup | Not started | Firestore cache first, Room later if required. |

## Next Implementation Order

1. Chat MVP finish.
2. Phone search and profile polish.
3. Block and report foundation. (Completed)
4. Notification fanout and deep-link foundation.
5. Media hardening with WorkManager (completed) and signed upload.
6. Group chat foundation.
7. One-to-one calling foundation.
8. Status/stories.
9. Channels.
10. Offline/cache/backup and personal beta hardening.

## Step Workflow

Each implementation step should follow this cycle:

1. Confirm Git status.
2. Give or perform the previous step commit/push action.
3. Implement one focused roadmap step.
4. Update docs when behavior or architecture changes.
5. Update `/learning` in Bangla with code-by-code explanation.
6. Run `assembleDebug` for app code changes.
7. Run targeted manual verification.
8. Provide next Git commands before the next step.

## Git Workflow

- Use small commits on the current feature branch unless a new major branch is intentionally created.
- Do not stage `apps/ContactMe/app/build.gradle.kts` unless it has a real visible diff.
- Recommended docs step commit:

```text
docs(roadmap): sync full app plan with current state
```

## Verification Workflow

For documentation-only steps:

```powershell
git diff --stat
git status --short --branch
```

For app implementation steps:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.11'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat assembleDebug
```

Run Gradle commands from:

```text
apps/ContactMe
```

## Next Recommended Step After This Documentation

Resume implementation with Chat MVP finish:

- Clean navigation state.
- Improve chat loading/empty/error states.
- Add send failure/retry foundation.
- Then move to typing/presence or phone search depending on priority.
