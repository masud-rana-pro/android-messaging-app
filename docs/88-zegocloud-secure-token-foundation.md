# Step 88: ZEGOCLOUD Configuration and Secure Token Backend

## Goal

Prepare authenticated, short-lived, room-scoped ZEGOCLOUD RTC credentials without shipping a server secret in the APK.

## Backend

- Added Firebase callable function `issueZegoCallToken` in `asia-south1`.
- Requires a valid Firebase Auth user.
- Accepts only a validated `callId`.
- Loads `calls/{callId}` and verifies the caller is in `participantIds`.
- Requires exactly two distinct participants plus an unexpired `ringing` or `accepted` call state.
- Uses the server-owned `roomId` rather than accepting an arbitrary room from Android.
- Generates a 10-minute Token04 scoped to login and publish in that room.
- Reads `ZEGO_SERVER_SECRET` from Firebase Secret Manager and `ZEGO_APP_ID` from a typed Firebase parameter.
- Returns only AppID, token, room ID, and expiry; no secret is returned.

## Android

- Added Firebase Functions Android dependency.
- Added regional `FirebaseFunctions` Hilt provider.
- Added `CallTokenRepository` and its callable-backed implementation.
- Validates call ID and token response before exposing a `CallToken`.
- No ZEGOCLOUD AppSign or ServerSecret is stored in the app.

## Step 89 Contract

The endpoint intentionally requires an existing `calls/{callId}` document. Step 89 will add the call session model, secure Firestore creation rules, participant IDs, and server-owned room ID contract. Until then a token request returns `not-found`.

## Manual Configuration Required

1. Create/select a ZEGOCLOUD RTC project and copy its numeric AppID and 32-byte ServerSecret.
2. Upgrade/select a Firebase plan that supports Cloud Functions and Secret Manager.
3. Install Firebase CLI, sign in, and confirm access to `messasing-app-9c367`.
4. Run `firebase functions:secrets:set ZEGO_SERVER_SECRET` and paste the ServerSecret only into the secure CLI prompt.
5. Deploy `issueZegoCallToken`; enter the numeric AppID when prompted for `ZEGO_APP_ID`.
6. Do not paste ServerSecret/AppSign into chat, source files, `google-services.json`, Firestore, or Android Studio.

## Verification

Low-token mode remains active, so Gradle/function build and deployment were not run. Targeted contract review and `git diff --check` were used.

## References

- ZEGOCLOUD official `zego_server_assistant` Token04 implementation: <https://github.com/ZEGOCLOUD/zego_server_assistant/tree/master/token/nodejs>
- Firebase callable functions: <https://firebase.google.com/docs/functions/callable>
- Firebase secret parameters: <https://firebase.google.com/docs/functions/config-env#secret_parameters>

## Commit

`feat(calls): add secure ZEGOCLOUD token foundation`
