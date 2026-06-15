# v0.1 UI Demo Verification

This document describes how to verify the current ContactMe UI demo.

## Build

From the Android project folder:

```powershell
cd apps\ContactMe
.\gradlew.bat assembleDebug
```

Expected:

```text
BUILD SUCCESSFUL
```

## Manual Flow

Run the app in Android Studio or install the debug APK.

Expected flow:

```text
Splash
-> Auth
-> Profile Setup
-> Home
-> Tap chat row
-> Chat Detail
-> Back
-> Home
-> Settings
-> Profile & Settings
-> Back
-> Home
```

## Screens To Check

### Splash

- ContactMe brand mark is centered.
- App moves to Auth screen automatically.

### Auth

- Login/Register buttons switch mode text.
- Email/phone and password fields are visible.
- Screen remains usable when keyboard opens.

### Profile Setup

- Photo placeholder is visible.
- Display name and username fields are visible.
- Continue opens Home.

### Home

- Top bar shows ContactMe.
- Settings action opens settings screen.
- Bottom tabs are visible:
  - Chats
  - Status
  - Calls
  - Groups
  - Channels

### Chat Detail

- Chat row opens detail screen.
- Chat name appears in top bar.
- Dummy message bubbles are visible.
- Back returns Home.

### Settings

- Profile placeholder is visible.
- Privacy, Notifications, Storage, and Blocked users entries are visible.
- Back returns Home.

## Known Limitations

- No real authentication.
- No Firebase connection.
- No real message sending.
- No local database.
- No notification/call/media implementation.

These limitations are expected for v0.1 UI Demo.
