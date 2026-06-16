# Phone Search Foundation

## Goal

Add phone-number discovery alongside username search so ContactMe moves closer to a WhatsApp-like identity model.

## What Changed

- `UserProfile` now includes `phoneNumber`.
- `ProfileRepository.searchProfiles()` now accepts a generic `query`.
- Profile save now stores the Firebase Auth user's:
  - `phoneNumber`
  - `email`
- Search now supports:
  - username prefix search
  - exact normalized Bangladesh phone-number search
- Discovery input now accepts `+` so E.164 phone numbers can be searched.
- Search placeholder now says `Search username or phone`.
- Search results show phone number when available.
- Firestore profile rules now require `phoneNumber` and `email` string fields.

## Phone Normalization

Supported Bangladesh input examples:

```text
01575634380 -> +8801575634380
8801575634380 -> +8801575634380
+8801575634380 -> +8801575634380
1575634380 -> +8801575634380
```

## Current Scope

This step supports phone search for profiles that already have `phoneNumber` saved.

Existing users may need to reopen/save profile once so their user document gets the new `phoneNumber` and `email` fields.

## Verification

1. Sign in with phone auth.
2. Save or update profile.
3. Confirm `users/{uid}.phoneNumber` exists in Firestore.
4. From another account, search the phone number.
5. Confirm the matching user appears.
6. Confirm username search still works.
7. Deploy updated Firestore rules.
8. Run `assembleDebug`.
