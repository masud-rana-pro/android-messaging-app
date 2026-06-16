# Last Seen Privacy Enforcement

## Goal

Apply the first saved privacy preference to real app behavior by hiding exact last-seen time when the peer chooses `Nobody`.

## What Changed

- Added `canShowLastSeen` to `PresenceStatus`.
- `FirebasePresenceRepository` now reads the peer user's `lastSeenVisibility`.
- Chat header shows exact `last seen h:mm AM/PM` only when `canShowLastSeen` is true.
- If last-seen is hidden, the header falls back to `last seen recently`.

## Current Scope

This step enforces only the `Nobody` value for last seen.

`Contacts` is treated as visible for now because native/contact-list relationship is not implemented yet.

## Verification

1. User B sets Last seen to `Nobody` in Settings.
2. User B goes offline.
3. User A opens chat with User B.
4. Confirm User A does not see exact last-seen time.
5. User B sets Last seen to `Everyone`.
6. Confirm exact last-seen time can appear again.
7. Run `assembleDebug`.
