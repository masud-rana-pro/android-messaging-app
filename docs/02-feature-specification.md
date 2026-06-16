# Feature Specification

This document tracks the current product surface and the remaining WhatsApp-like feature target for ContactMe.

## Product Direction

ContactMe is an Android-first messaging app inspired by WhatsApp-style flows. It should support real user identity, realtime direct chat, media, groups, calls, status, channels, notifications, privacy, and a personal beta release path.

## Implemented Foundation

- Splash, auth, profile setup, home tabs, chat detail, settings, and placeholder secondary tabs.
- Firebase Auth with email/password and phone OTP flow.
- Phone number normalization for Bangladesh-style input.
- Session restore and logout.
- Firestore user profile save/load.
- Username uniqueness and username discovery.
- Direct one-to-one conversation creation.
- Real text messages stored under conversation subcollections.
- Firestore conversation list with last message preview.
- Chat detail timestamp and auto-scroll.
- Conversation list timestamp and unread/read foundation.
- Message sent status foundation.
- Firestore MVP security rules for current chat/auth/profile flows.

## Current Partial Areas

- Chat UI is functional but still needs WhatsApp-like polish, better navigation state, retry, typing, presence, and message actions.
- Contact discovery supports username; phone search is planned next.
- Profile has persisted display name/username; profile photo and privacy settings are still planned.
- Message status supports `sent`; delivered/read receipts require real recipient state before UI shows them.
- Firestore rules cover MVP text chat; media, block/report, groups, calls, and emulator tests are still planned.

## Not Implemented Yet

- Native contacts sync.
- Profile photo upload.
- Phone-number user search.
- Typing indicator and online/last-seen presence.
- Message actions: reply, forward, star, pin, edit, delete, reactions.
- Media messages: image, video, document, audio, voice note.
- Push notifications and notification deep links.
- Groups and communities.
- Voice/video calls.
- Status/stories.
- Channels.
- Offline queue, Room cache, backup/export.
- Admin/moderation dashboard.
- Real E2EE.

## Core Modules

- Auth and identity.
- Contacts and discovery.
- One-to-one chat.
- Chat list and unread state.
- Message actions and status.
- Media messaging.
- Groups and communities.
- Status/stories.
- Channels.
- Calls.
- Notifications.
- Privacy and security.
- Backup/offline.
- Admin/support.

## Step Rule

Every new feature step must include implementation, verification, docs, learning notes, and Git instructions before moving to the next feature.
