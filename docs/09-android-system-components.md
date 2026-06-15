# Android System Components Plan

This document defines which Android platform components ContactMe should use, when to introduce them, and which features they support.

## Principle

Do not add Android system components just because they exist. Add them only when a feature needs lifecycle, background execution, inter-app sharing, notification, or OS integration.

## Phase Summary

| Phase | Component | Use |
| --- | --- | --- |
| v0.1 UI Demo | Activity only | Compose screen flow and UI prototype |
| v0.2 Auth Build | Firebase Auth + normal app lifecycle | Login, register, session restore |
| v0.3 Chat MVP | Firestore listeners + local state | Realtime one-to-one text chat |
| v0.4 Chat Plus | Notification channel preparation | Message status/actions foundation |
| v0.5 Media | FileProvider + WorkManager | Attachment sharing and reliable media upload |
| v0.6 Notifications | FirebaseMessagingService + notification channels | Message/call push notifications |
| v0.7 Calling | Foreground Service + notification actions | Active call, ringing, accept/reject |
| v0.8+ Groups/Status/Channels | WorkManager | sync, retry, cleanup, scheduled tasks |
| v2.0 Production Candidate | Room + WorkManager + lifecycle observers | offline cache, backup, sync resilience |

## Activity

### Current Use

- `MainActivity`
- Jetpack Compose UI host

### Needed From

- v0.1 UI Demo

### Responsibility

- Start the app.
- Attach Compose UI through `setContent`.
- Host navigation/screen flow.

### Rule

Keep `MainActivity` thin. Do not put chat, auth, notification, upload, or call logic directly inside the Activity.

## ContentProvider

### Custom ContentProvider

Not needed for early ContactMe phases.

### FileProvider

Likely needed from v0.5 Media.

### Use Cases

- Share image/document/audio files with other apps.
- Attach camera/gallery/file picker output safely.
- Expose temporary content URI instead of raw file paths.

### Why FileProvider

Android does not allow exposing raw `file://` paths to other apps safely. `FileProvider` creates controlled `content://` URIs.

### Do Not Add Yet

Do not add FileProvider before media/attachment features start.

## BroadcastReceiver

### Needed Carefully

Broadcast receivers should be used only for specific OS or notification events.

### Likely Use Cases

- Call notification actions:
  - accept call
  - reject call
  - mark missed call
- Boot completed restoration for scheduled background work, only if future sync/backup requires it.
- Notification action handling if a dedicated receiver is cleaner than Activity deep links.

### Avoid

- Do not use a receiver for continuous network monitoring.
- Do not rely on implicit background broadcasts for realtime chat.

### Preferred Alternative

For chat and presence, use:

- Firestore listeners while app is active.
- Realtime Database presence.
- WorkManager for retryable background work.
- FCM for server-triggered events.

## Service

### Normal Background Service

Avoid for most features. Modern Android restricts background services heavily.

### Foreground Service

Likely needed from v0.7 Calling.

### Use Cases

- Active voice/video call.
- Ongoing call notification.
- Call controls while app is backgrounded.
- Possibly long-running voice note recording if background recording is supported.

### Rule

Use foreground service only when the user is actively aware of the task, such as an ongoing call.

## FirebaseMessagingService

### Needed From

- v0.6 Notifications

### Use Cases

- Receive FCM token.
- Sync token to backend/user device record.
- Handle foreground push messages.
- Show message, group, channel, and call notifications.

### Supported Notification Types

- Direct message
- Group message
- Incoming call
- Missed call
- Channel post
- Status interaction, if later needed

## WorkManager

### Needed From

- v0.5 Media for upload retry
- v0.6+ Notifications/token sync
- v2.0 Offline/backup production work

### Use Cases

- Retry failed message send.
- Retry failed media upload.
- Cleanup expired status cache.
- Sync FCM token.
- Periodic media cleanup.
- Future backup/sync tasks.

### Why WorkManager

WorkManager is the preferred Android API for deferrable, guaranteed background work.

### Rule

Use WorkManager for work that can finish later. Do not use it for active calls or realtime chat listeners.

## Room Database

### Needed From

- v2.0 Production Candidate
- Can be introduced earlier if offline chat becomes a v1 requirement

### Use Cases

- Local chat list cache.
- Last message cache.
- Offline message send queue.
- Media upload state.
- Sync cursors.

### Rule

Do not introduce Room before the Firestore message model is stable.

## Notification Channels

### Needed From

- v0.6 Notifications

### Channels

| Channel | Purpose | Importance |
| --- | --- | --- |
| messages | one-to-one chat messages | high |
| groups | group chat messages | high |
| calls | incoming/ongoing calls | high/call style |
| channels | channel post updates | default |
| status | status/story interactions | low/default |
| system | account/session/system notices | default |

### Rule

Create notification channels once at app startup before showing notifications.

## Permissions Plan

| Feature | Permission | Phase |
| --- | --- | --- |
| Notifications | `POST_NOTIFICATIONS` | v0.6 |
| Camera attachment/profile photo | `CAMERA` | v0.5/v0.2 profile photo |
| Microphone voice note/call | `RECORD_AUDIO` | v0.5 voice note, v0.7 call |
| Video call | `CAMERA`, `RECORD_AUDIO` | v0.7 |
| Media picker | Android Photo Picker preferred | v0.5 |
| Legacy external storage | Avoid unless absolutely needed | avoid |
| Contacts sync | `READ_CONTACTS` only if native contacts sync is implemented | v0.3+ optional |

## Deep Links

### Needed From

- v0.6 Notifications

### Use Cases

- Open a specific chat from notification.
- Open incoming/missed call screen.
- Open channel post.
- Open report/help screen.

### Rule

Design deep link routes before implementing complex notifications.

## Feature-to-Component Map

| Feature | Android Component |
| --- | --- |
| Splash/Auth/Home UI | Activity + Compose |
| Profile photo upload | Photo Picker/FileProvider + Firebase Storage |
| Text chat | Firestore listener, ViewModel, Flow |
| Media message | FileProvider, WorkManager, Firebase Storage |
| Voice note | media recorder APIs, optional foreground handling if background recording |
| Push notification | FirebaseMessagingService, notification channels |
| Incoming call | FirebaseMessagingService, Foreground Service, notification actions |
| Active call | Foreground Service, ZegoCloud SDK |
| Offline retry | WorkManager, later Room |
| Backup | WorkManager, later Room/cloud backup |
| Contacts sync | optional Contacts provider access |

## Implementation Order

1. Keep v0.1 UI Demo Activity/Compose only.
2. Add ViewModel/Hilt before real Auth implementation.
3. Add Firebase Auth for v0.2.
4. Add Firestore chat listeners for v0.3.
5. Add FileProvider only when media attachments start.
6. Add WorkManager when upload/retry tasks start.
7. Add FirebaseMessagingService and notification channels for v0.6.
8. Add Foreground Service only when calling starts.
9. Add Room when offline/sync requirements are stable.

## Things To Avoid Early

- Custom ContentProvider without a strong reason.
- Long-running background Service for chat.
- BroadcastReceiver for realtime chat.
- Room database before message schema stabilizes.
- E2EE claim before real cryptographic implementation and testing.

