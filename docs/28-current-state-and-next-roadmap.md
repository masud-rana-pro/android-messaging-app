# Current State and Next Roadmap

This document is the implementation checkpoint after the message status foundation. It aligns the old full roadmap with the actual repository state.

## Project Truth

- App name: ContactMe.
- Platform: Android first.
- Android project path: `apps/ContactMe`.
- Backend strategy: Firebase Spark for auth/data, Cloudinary for current media upload, and no-card WebRTC calling.
- Current branch: `feature/auth-build`.
- Current release direction: personal beta APK before Play Store hardening.
- UI direction: WhatsApp-like custom ContactMe theme, not a trademark clone.
- Learning rule: every implementation step must update `/learning` with detailed Bangla explanation.

## Completed Feature Matrix

| Area | Status | Notes |
| --- | --- | --- |
| Android scaffold | Done | Kotlin, Compose, Material 3, Hilt. |
| Firebase setup | Done | Auth and Firestore are connected. |
| Phone auth | Done foundation | Firebase Phone Auth OTP path exists; polished with test-number instructions. |
| Email fallback | Done foundation | Email/password login/register path exists; polished as primary test path. |
| Session restore/logout | Done | App can restore Firebase session and sign out. |
| Profile persistence | Done foundation | Profile data saves/loads from Firestore; polished with uniqueness checks. |
| Username uniqueness | Done foundation | Username reservation and search exist. |
| Direct conversation | Done foundation | Direct conversation document can be created. |
| Text messages | Done foundation | Messages send/render from Firestore. |
| Conversation list | Done foundation | Firestore conversation previews exist. |
| Unread/read | Done MVP | Conversation-level unread state and privacy-aware peer read receipt exist. |
| Message status | Done MVP | Sent state plus peer-read timestamp rendering exist; separate delivered acknowledgement remains later. |
| Profile photo | Done MVP | Photo Picker uploads image to Cloudinary and stores `photoUrl` in Firestore. |
| Media messages | Done MVP | Chat images upload to Cloudinary and store metadata in Firestore. |
| Typing/presence | Done foundation | Firestore typing and Realtime Database presence foundations exist. |
| FCM token sync | Done foundation | Android token syncs to `user_devices/{uid}/devices/{deviceId}`. |
| Notification channels | Done foundation | Messages, calls, and system channels are created. |
| Notification permission | Done foundation | Android 13+ `POST_NOTIFICATIONS` runtime request exists. |
| Notification renderer | Done foundation | Foreground FCM payloads can render Android notifications. |
| Firestore rules | Done MVP | Rules cover current user/profile/direct chat/device-token flows; hardened to prevent crashes on permission denial. |

## Partial Or Not Started

| Area | Status | Next Need |
| --- | --- | --- |
| Chat UI polish | Personal-beta code complete | Core text/media chat, actions, sync retry, notification navigation, and secure read receipts exist; deployment and two-device validation remain. |
| Phone search | Done foundation | Normalize and query phone identity; polished with local contacts matching and dedicated Start Chat screen. |
| Privacy settings | Done foundation | Last seen/profile photo/read receipt settings exist; more hardening remains. |
| Block/report | Done foundation | Data model, rules, chat enforcement, reason selection, block/unblock UI; emulator tests and moderation tools remain. |
| Notifications | Client foundation | FCM rendering and chat deep links exist; future trusted fanout is Cloudflare Worker + FCM, not Firebase Functions. |
| Media polish | Advanced foundation | Validation, compression, preview, retry, app-private pending files, and WorkManager background delivery exist; signed production upload remains. |
| Groups | Working MVP | Creation UI, member selection, group previews, text/image messages, sender labels, notification routing, admin schema, validation, and rules exist; member/admin management and group profile editing remain. |
| Calls | Step 93.5 UI verification complete | Firestore session/state API, atomic offer/accept, role-specific ICE paths, strict rules, WebRTC engine, STUN, and optional local TURN config exist; Step 90-91 calling flows implemented; Step 92 background notification implemented; Step 93 audio controls and foreground service implemented; Step 93.5 UI verification and testability polished; Step 94 video call remains. |
| Status/channels | Not started | Media/status/channel models and UI. |
| Offline/backup | Not started | Firestore cache first, Room later if required. |

## Next Implementation Order

1. Chat MVP finish.
2. Phone search and profile polish.
3. Block and report foundation. (Completed)
4. Notification deep-link foundation and future Cloudflare Worker fanout.
5. Media hardening with WorkManager (completed) and signed upload.
6. Group chat foundation.
7. One-to-one WebRTC calling foundation.
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

Step 93: Microphone, speaker, mute and foreground service polish.
