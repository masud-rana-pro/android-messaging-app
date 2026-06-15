# Profile Persistence

ContactMe now saves basic user profile data in Firestore.

## Current Flow

```text
Auth success
-> Profile Setup
-> Save display name and username
-> Firestore users/{uid}
-> Home
```

App launch:

```text
Splash
-> signed out: Auth
-> signed in + profile incomplete: Profile Setup
-> signed in + profile complete: Home
```

## Firestore Document

Collection:

```text
users/{firebaseAuthUid}
```

Fields:

```text
displayName: string
username: string
profileComplete: boolean
createdAt: server timestamp
updatedAt: server timestamp
```

## Required Firebase Console Setup

1. Open Firebase Console.
2. Open Firestore Database.
3. Create a database.
4. Start in test mode for local development only, or add authenticated-user rules.

Development rule idea:

```text
allow read, write: if request.auth != null && request.auth.uid == userId;
```

## Current Limitations

- Username uniqueness is not checked yet.
- Profile photo upload is not implemented yet.
- Settings screen does not load saved profile data yet.
- Firestore security rules must be configured in Firebase Console.

## Verify

1. Sign in.
2. Enter display name and username.
3. Tap Save and continue.
4. Confirm Home opens.
5. Check Firebase Console -> Firestore -> users -> current uid.
6. Reopen app.
7. Splash should go directly to Home when `profileComplete` is true.
