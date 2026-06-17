# Step 50 - Settings Profile Photo Preview

## Goal

Show the saved profile photo in the Settings profile header after a successful profile photo upload.

## What Changed

- Added `photoUrl` to `SettingsUiState`.
- Mapped `UserProfile.photoUrl` into settings state in `SettingsViewModel`.
- Updated Settings avatar rendering:
  - show `AsyncImage` when `photoUrl` exists
  - fall back to initials when no photo is saved

## Verification

1. Upload a profile photo from profile edit/setup.
2. Return to Settings.
3. Confirm the profile header avatar shows the uploaded photo.
4. If the photo does not appear instantly, leave Settings and open it again so the ViewModel reloads the latest Firestore profile.
5. Run `./gradlew.bat assembleDebug`.
