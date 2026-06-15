# v0.2 Auth Build Plan

This document tracks the v0.2 authentication phase.

## Goal

Move from placeholder auth UI toward real authentication while keeping the codebase testable and maintainable.

## Current Step Completed

Auth foundation has been added:

- Hilt dependency injection setup
- `ContactMeApplication`
- `@AndroidEntryPoint` MainActivity
- Auth repository interface
- Fake auth repository
- Auth UI state model
- Auth ViewModel
- ViewModel-driven Auth screen

## Current Auth Flow

```text
AuthScreen
-> AuthViewModel
-> AuthRepository
-> FakeAuthRepository
-> Success/Error
```

## Why Fake Repository First

Firebase Auth should not be wired directly into the composable screen.

The fake repository allows the app to:

- validate UI state flow
- test loading/error/success states
- keep Firebase replaceable
- avoid backend dependency while building architecture

## Next Steps

1. Add Firebase Gradle plugin and dependencies.
2. Add Firebase project config when available.
3. Implement FirebaseAuthRepository.
4. Replace fake binding with Firebase binding.
5. Add session restore.
6. Save profile data after auth.

## Not Done Yet

- Real Firebase Auth
- OTP/phone auth
- Email verification
- Session restore
- Firestore user profile save
