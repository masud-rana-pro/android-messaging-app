# Settings Profile Load

ContactMe now loads saved profile data on the Settings screen.

## Current Flow

```text
Home
-> Settings
-> load users/{uid}
-> show displayName, username, initials
```

Edit profile foundation:

```text
Settings
-> Edit profile
-> Profile Setup
-> existing profile is prefilled
-> Save changes
```

## Changed Code

```text
profile/UserProfile.kt
profile/ProfileRepository.kt
profile/FirebaseProfileRepository.kt
profile/FakeProfileRepository.kt
ui/settings/SettingsUiState.kt
ui/settings/SettingsViewModel.kt
ui/screens/SettingsScreen.kt
ui/profile/ProfileSetupUiState.kt
ui/profile/ProfileSetupViewModel.kt
ui/screens/ProfileSetupScreen.kt
ui/ContactMeApp.kt
```

## Verify

1. Sign in and save profile.
2. Open Settings.
3. Confirm saved display name and username appear.
4. Tap Edit profile.
5. Confirm Profile Setup opens with saved data prefilled.
6. Change profile and save.
7. Reopen Settings and confirm updated data appears.
