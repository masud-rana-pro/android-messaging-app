# Session Restore And Logout

ContactMe now has a basic authenticated session flow.

## Current Flow

```text
App launch
-> Splash
-> check Firebase currentUser
-> signed in: Profile Setup
-> signed out: Auth
```

Logout:

```text
Home
-> Settings
-> Log out
-> Firebase signOut
-> Auth
```

## Why Signed-In Users Go To Profile Setup

Profile persistence is not implemented yet. Until the app can save and load profile completion state, signed-in users skip Auth but still land on Profile Setup.

Future flow:

```text
signed in + profile complete -> Home
signed in + profile incomplete -> Profile Setup
signed out -> Auth
```

## Changed Code

```text
auth/AuthRepository.kt
auth/FirebaseAuthRepository.kt
auth/FakeAuthRepository.kt
ui/session/SessionViewModel.kt
ui/ContactMeApp.kt
ui/screens/SettingsScreen.kt
```

## Verify

1. Sign in with phone or email.
2. Close and reopen the app.
3. Splash should skip Auth and open Profile Setup.
4. Go to Home, then Settings.
5. Tap Log out.
6. App should return to Auth.
