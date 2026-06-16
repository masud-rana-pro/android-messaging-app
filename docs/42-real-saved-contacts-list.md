# Step 42 - Real Saved Contacts List

## Goal

Remove the new contact fake implementation and show real saved contacts from Firestore inside the Chats screen.

## What Changed

- Removed `FakeContactRepository`.
- Added `ContactRepository.observeContacts(ownerUserId)`.
- `FirebaseContactRepository` now observes `contacts/{uid}/items` in real time.
- Contacts are hydrated from current `users/{contactUid}` profile documents.
- Contact profile photos respect `profilePhotoVisibility`.
- The Chats screen now shows a real Saved contacts section.
- Dummy chat preview data was removed from the Chats screen.
- The Chats content is vertically scrollable.

## Verification Checklist

1. Build the app with `./gradlew.bat assembleDebug`.
2. Deploy Firestore rules if not already deployed.
3. Search a real user and open a chat.
4. Confirm `contacts/{currentUid}/items/{otherUid}` exists in Firestore.
5. Return to Chats.
6. Confirm the user appears under Saved contacts.
7. Tap the saved contact and confirm the real direct conversation opens.

## Note

Older `Fake...Repository` classes still exist from earlier development steps, but this new contacts feature now uses only the real Firebase repository.
