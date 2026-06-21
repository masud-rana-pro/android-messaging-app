# Firestore PERMISSION_DENIED Troubleshooting

If you encounter `com.google.firebase.firestore.FirebaseFirestoreException: PERMISSION_DENIED` in the application, follow this guide to resolve it.

## Root Cause

1.  **Missing Rules:** The security rules have not been deployed to the Firebase Console.
2.  **Stale Rules:** The local `firestore.rules` file is updated, but the Firebase Console is still using an older, more restrictive version.
3.  **Authenticated User Mismatch:** The rule expects `request.auth.uid` to be in a certain field (like `participantIds` or `callerId`), but it is missing or different.
4.  **Schema Validation Failure:** The update includes fields that are forbidden, or misses fields that are required by the rule's `request.resource.data` checks.

## Resolution Steps

### 1. Repository Fortification (App Side)

The repositories in `ContactMe` have been fortified with `runCatching` blocks and safe listener defaults. This prevents the app from crashing even if Firestore denies access.

-   `FirebaseConversationRepository.kt`: Safe observation of previews and read receipts.
-   `FirebaseMessageRepository.kt`: Safe message observation and sender name resolution.
-   `FirebaseCallSignalingRepository.kt`: Safe listener defaults for incoming calls and ICE candidates.
-   `FirebaseSafetyRepository.kt`: Safe peer lookup.

**Note:** While the app won't crash, the data will simply be empty or operations will fail silently or with a user-friendly error message until rules are fixed.

### 2. Deploy Rules (Manual Step)

You must deploy the updated rules from `firebase/firestore.rules` to your Firebase project.

```bash
# From project root
firebase deploy --only firestore:rules
```

Or copy the contents of `firebase/firestore.rules` directly into the **Rules** tab of your Firestore Database in the Firebase Console.

### 3. Verify Rules Alignment

Ensure the rules match the collection structure:
-   `users/{uid}`: Profile data.
-   `conversations/{id}`: Participant IDs must be an array of UIDs.
-   `calls/{id}`: Caller and Receiver IDs must be UIDs.

### 4. Check for Blocked Access

If you are trying to read a document and it returns `PERMISSION_DENIED`, check if you are a participant. For example, in a direct chat, the `participantIds` must contain your UID.

## Known Limitations

-   The app does not allow broad read access to `blocked_users` for privacy. However, a user can check if they themselves are blocked by another user to allow the safety logic to function.
-   ICE candidates are strictly restricted to the caller and receiver of the specific call session.
