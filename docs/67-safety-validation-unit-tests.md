# Step 67: Safety Validation Unit Tests

## Goal

Protect the block, unblock, and report input rules with fast Firebase-independent unit tests.

## Implementation

- Added `SafetyInputValidator` as the single source for safety input validation.
- `FirebaseSafetyRepository` delegates validation before any Firestore request.
- Added JUnit 4 through the existing version catalog.
- Added six local tests for blank IDs, self-actions, and valid peer actions.

## Verification

Run:

```bash
./gradlew testDebugUnitTest assembleDebug
```

The test task and debug APK build must both succeed.

## Commit

`test(safety): cover block and report validation`
