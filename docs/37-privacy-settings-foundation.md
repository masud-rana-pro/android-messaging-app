# Privacy Settings Foundation

## Goal

Add the first privacy settings model and Settings screen controls for WhatsApp-like account privacy.

## What Changed

- Added `PrivacyVisibility`.
- Added `PrivacySettings`.
- `ProfileRepository` can now load and save privacy settings.
- Firebase implementation stores privacy settings in `users/{uid}`.
- Fake implementation supports the same interface.
- Settings UI now shows controls for:
  - Last seen visibility
  - Profile photo visibility
  - Read receipts
- Firestore rules validate privacy fields when present.

## Current Firestore Fields

```text
users/{uid}
  lastSeenVisibility: "everyone" | "contacts" | "nobody"
  profilePhotoVisibility: "everyone" | "contacts" | "nobody"
  readReceiptsEnabled: true | false
```

## Current Scope

This step saves privacy preferences. Enforcement is planned in later steps.

For example:

- Last seen visibility is not yet applied to the chat header.
- Profile photo visibility is not yet applied to avatar reads.
- Read receipt preference is not yet applied to read marker behavior.

## Verification

1. Open Settings.
2. Tap Last seen and confirm it cycles through Everyone, Contacts, Nobody.
3. Tap Profile photo and confirm it cycles through Everyone, Contacts, Nobody.
4. Toggle Read receipts.
5. Confirm Firestore `users/{uid}` updates the privacy fields.
6. Run `assembleDebug`.
7. Deploy Firestore rules.
