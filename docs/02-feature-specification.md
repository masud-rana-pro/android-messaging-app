# Feature Specification

## v0.1 UI Demo Scope

The current UI demo is a non-backend prototype. It validates ContactMe's primary screen flow before Firebase/Auth/Chat implementation begins.

### Implemented Placeholder Screens

- Splash
- Auth login/register placeholder
- Profile setup placeholder
- Home with bottom tabs
- Chats tab with dummy conversations
- Chat detail placeholder
- Status tab placeholder
- Calls tab placeholder
- Communities/Groups tab placeholder
- Channels tab placeholder
- Profile and settings placeholder

### Current Demo Flow

```text
Splash -> Auth -> Profile Setup -> Home
Home Chats -> Chat Detail -> Back -> Home
Home Settings -> Profile & Settings -> Back -> Home
```

### Not Implemented Yet

- Real Firebase Auth
- Real user profile persistence
- Real contacts search/sync
- Real Firestore conversation list
- Message send/receive
- Media upload
- Push notifications
- Calls
- Groups/status/channels functionality

## Core Modules

- Auth and identity
- Contacts
- One-to-one chat
- Message actions
- Chat list
- Groups
- Communities
- Status/stories
- Channels
- Calls
- Notifications
- Privacy and security
- Backup/offline
- Admin/support
