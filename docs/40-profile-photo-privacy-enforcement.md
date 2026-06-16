# Step 40 - Profile Photo Privacy Enforcement

## Goal

Apply the existing profile photo privacy setting to user-facing discovery and chat list surfaces.

## What Changed

- Search results now hide another user's `photoUrl` when that user's `profilePhotoVisibility` is `nobody`.
- Conversation previews now hide the peer user's `photoUrl` when the peer selected `nobody`.
- The user's own profile loading still keeps the raw `photoUrl`, so profile edit/settings screens can continue to show the owner's photo.
- Existing avatar UI already falls back to initials when `photoUrl` is blank.

## Current Privacy Behavior

| Setting | Current behavior |
| --- | --- |
| `everyone` | Other users can see the profile photo. |
| `contacts` | Treated as visible for now because the contact relationship model is not implemented yet. |
| `nobody` | Other users see initials fallback instead of the profile photo. |

## Files Updated

- `apps/ContactMe/app/src/main/java/com/contactme/app/profile/FirebaseProfileRepository.kt`
- `apps/ContactMe/app/src/main/java/com/contactme/app/conversation/FirebaseConversationRepository.kt`
- `learning/40-profile-photo-privacy-enforcement.md`

## Verification Checklist

1. Build the Android app with `./gradlew.bat assembleDebug`.
2. User B uploads a profile photo.
3. User B sets profile photo visibility to `Nobody`.
4. User A searches for User B by username or phone.
5. User A should see initials instead of User B's photo.
6. User A opens or returns to the conversation list.
7. User A should still see initials instead of User B's photo.
8. User B changes visibility back to `Everyone`.
9. User A should see the photo again after data refresh.

## Next Step

The next privacy improvement should add the contact relationship model, so `contacts` can be enforced differently from `everyone`.
