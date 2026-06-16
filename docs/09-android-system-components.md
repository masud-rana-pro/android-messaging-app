# Android System Components Plan

This document defines which Android platform components ContactMe should use, when to introduce them, and which features they support.

## Principle

Do not add Android system components just because they exist. Add them only when a feature needs lifecycle, background execution, inter-app sharing, notification, or OS integration.

## Current State

The app currently uses:

- `MainActivity` as the Compose host.
- Firebase Auth.
- Firestore listeners.
- Hilt ViewModels and repositories.

The app does not yet use:

- FileProvider.
- BroadcastReceiver.
- Foreground Service.
- FirebaseMessagingService.
- WorkManager.
- Room.

## Phase Summary

| Phase | Component | Use |
| --- | --- | --- |
| Current checkpoint | Activity + Compose + Firebase Auth/Firestore | Auth, profile, direct text chat. |
| Chat MVP finish | Firestore listeners + local state | Typing, unread, better message state. |
| Contacts/Profile | Android Photo Picker, later FileProvider if needed | Profile photo and safe media URI handling. |
| Notifications | FirebaseMessagingService + notification channels | Message/call push notifications and deep links. |
| Media | Firebase Storage + WorkManager + FileProvider when sharing/camera requires it | Reliable attachment upload and retry. |
| Groups | Firestore listeners + notification channels | Group chat and group notifications. |
| Calling | Foreground Service + notification actions | Active call, ringing, accept/reject. |
| Status/Channels | WorkManager where useful | Status cleanup, channel sync, retry tasks. |
| Hardening | Room + WorkManager if needed | Offline cache, backup, sync resilience. |

## Activity

### Current Use

- `MainActivity`.
- Jetpack Compose UI host.

### Responsibility

- Start the app.
- Attach Compose UI through `setContent`.
- Host the root Compose app.

### Rule

Keep `MainActivity` thin. Do not put auth, chat, notification, upload, or call logic directly inside the Activity.

## ContentProvider

### Custom ContentProvider

Not needed for early ContactMe phases.

### FileProvider

Needed only when the media/profile-photo feature requires safe file sharing with camera, file picker output, or other apps.

### Why FileProvider

Android should not expose raw `file://` paths to other apps. `FileProvider` creates controlled `content://` URIs.

### Do Not Add Yet

Do not add FileProvider before media/profile photo implementation needs it.

## BroadcastReceiver

Broadcast receivers should be used only for specific OS or notification events.

### Likely Use Cases

- Call notification actions:
  - accept call
  - reject call
  - mark missed call
- Boot restoration only if future sync/backup genuinely needs it.

### Avoid

- Do not use a receiver for continuous network monitoring.
- Do not rely on implicit background broadcasts for realtime chat.

## Service

### Normal Background Service

Avoid for most features. Modern Android restricts background services heavily.

### Foreground Service

Needed from the calling phase.

### Use Cases

- Active voice/video call.
- Ongoing call notification.
- Call controls while app is backgrounded.

### Rule

Use a foreground service only when the user is actively aware of the task, such as an ongoing call.

## FirebaseMessagingService

### Needed From

Notifications phase.

### Use Cases

- Receive FCM token.
- Sync token to `user_devices/{uid}/devices/{deviceId}`.
- Handle foreground push messages.
- Show message, group, channel, and call notifications.

### Supported Notification Types

- Direct message.
- Group message.
- Incoming call.
- Missed call.
- Channel post.
- System notice.

## WorkManager

### Needed From

- Media phase for upload retry.
- Notifications phase for token sync retry if needed.
- Hardening phase for offline/backup cleanup.

### Use Cases

- Retry failed media upload.
- Sync FCM token.
- Cleanup expired status cache.
- Periodic media cleanup.
- Future backup/sync tasks.

### Rule

Use WorkManager for work that can finish later. Do not use it for active calls or realtime chat listeners.

## Room Database

### Needed From

Hardening/offline phase, or earlier only if Firestore cache is not enough.

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

Notifications phase.

### Channels

| Channel | Purpose | Importance |
| --- | --- | --- |
| messages | One-to-one chat messages | High |
| groups | Group chat messages | High |
| calls | Incoming/ongoing calls | High/call style |
| channels | Channel post updates | Default |
| status | Status/story interactions | Low/default |
| system | Account/session/system notices | Default |

### Rule

Create notification channels once at app startup before showing notifications.

## Permissions Plan

| Feature | Permission | Phase |
| --- | --- | --- |
| Notifications | `POST_NOTIFICATIONS` | Notifications |
| Camera attachment/profile photo | `CAMERA` only if camera capture is implemented | Contacts/Profile or Media |
| Microphone voice note/call | `RECORD_AUDIO` | Voice note or Calling |
| Video call | `CAMERA`, `RECORD_AUDIO` | Calling |
| Media picker | Android Photo Picker preferred | Profile photo and Media |
| Legacy external storage | Avoid unless absolutely needed | Avoid |
| Contacts sync | `READ_CONTACTS` only if native contacts sync is implemented | Later optional |

## Deep Links

### Needed From

Notifications phase.

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
| Profile photo upload | Photo Picker/FileProvider if needed + Firebase Storage |
| Text chat | Firestore listener, ViewModel, Flow |
| Media message | Photo Picker/FileProvider, WorkManager, Firebase Storage |
| Voice note | Media recorder APIs, optional foreground handling if background recording is supported |
| Push notification | FirebaseMessagingService, notification channels |
| Incoming call | FirebaseMessagingService, Foreground Service, notification actions |
| Active call | Foreground Service, ZegoCloud SDK |
| Offline retry | WorkManager, later Room |
| Backup | WorkManager, later Room/cloud backup |
| Contacts sync | Optional Contacts provider access |

## Implementation Order

1. Finish direct chat MVP.
2. Add phone search, profile photo, privacy, block/report.
3. Add notification channels and FCM service.
4. Add media attachments and WorkManager retry.
5. Add groups.
6. Add ZegoCloud one-to-one calls and foreground service.
7. Add status/channels.
8. Add Room only when offline/cache needs are stable.

## Things To Avoid Early

- Custom ContentProvider without a strong reason.
- Long-running background service for chat.
- BroadcastReceiver for realtime chat.
- Room database before message schema stabilizes.
- E2EE claim before real cryptographic implementation and testing.
