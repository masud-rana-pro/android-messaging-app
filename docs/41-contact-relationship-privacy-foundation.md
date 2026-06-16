# Step 41 - Contact Relationship Privacy Foundation

## Goal

Add the first contact relationship model so `contacts` privacy can behave differently from `everyone`.

## What Changed

- Added `ContactRepository` with a Firebase implementation.
- Opening a direct chat from discovery now saves the peer as a contact for the current user.
- Profile photo privacy now enforces:
  - `everyone`: visible
  - `contacts`: visible only when the viewer is in the profile owner's contact list
  - `nobody`: hidden
- Last seen privacy now uses the same contacts-only rule.
- Firestore rules now include `contacts/{uid}/items/{contactUid}`.
- Database and security docs now describe the implemented contact model.

## Data Model

```text
contacts/{ownerUid}/items/{contactUid}
  userId
  displayName
  username
  phoneNumber
  photoUrl
  updatedAt
```

## Privacy Meaning

For a profile owner `B` and viewer `A`, `contacts` visibility is allowed only if this document exists:

```text
contacts/B/items/A
```

This follows the product meaning that a user controls what their own saved contacts can see.

## Verification Checklist

1. Build the app with `./gradlew.bat assembleDebug`.
2. Deploy Firestore rules before testing on real Firebase.
3. User A searches User B and opens a direct chat.
4. Confirm `contacts/A/items/B` is created.
5. If User B sets last seen/profile photo to `Contacts`, User A should see those only after User B also has `contacts/B/items/A`.
6. If User B sets either setting to `Nobody`, User A should not see it.

## Next Step

Add a visible contacts screen or saved contacts list so users can manage contacts directly instead of relying only on chat-open auto-save.
