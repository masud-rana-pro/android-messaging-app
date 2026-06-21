# Step 92: Background Incoming Call Notification

Implemented the background/killed-app notification path for incoming calls using Cloudflare Worker as a secure FCM bridge.

## Architecture

1.  **FCM Token Mirroring**: Android app mirrors the latest FCM token to `users/{uid}/fcmToken` for easy server-side lookup.
2.  **Worker Trigger**: `OutgoingCallViewModel` triggers a POST request to the Cloudflare Worker after successfully creating a call offer in Firestore.
3.  **Cloudflare Worker**:
    *   Authenticates with Google OAuth using Firebase Service Account secrets (stored in Cloudflare).
    *   Reads the receiver's FCM token from Firestore.
    *   Sends a high-priority data-only FCM message to the receiver.
4.  **Android Handling**:
    *   `ContactMeMessagingService` receives the `incoming_call` data payload.
    *   `ContactMeNotificationRenderer` creates a high-priority notification with a deep-link to the `IncomingCallScreen`.
    *   `MainActivity` and `ContactMeApp` parse the `callId` from the notification tap and navigate to the call screen.

## Key Files Created/Modified

### Android
*   `FirebaseDeviceTokenRepository.kt`: Added mirroring to user document.
*   `OutgoingCallViewModel.kt`: Added Worker trigger.
*   `NotificationNavigation.kt`: Added `callId` parsing.
*   `ContactMeNotificationPayload.kt` & `Renderer.kt`: Added call notification support.

### Backend
*   `backend/cloudflare-worker/src/index.js`: Core worker logic with JWT signing for Google Auth.
*   `backend/cloudflare-worker/wrangler.toml`: Worker configuration.

## Manual Configuration Required
*   Firebase Service Account JSON content must be set as Cloudflare secrets:
    *   `FIREBASE_PROJECT_ID`
    *   `FIREBASE_CLIENT_EMAIL`
    *   `FIREBASE_PRIVATE_KEY` (with newlines preserved)

## Boundaries
*   Foreground service and full-screen notification polish are scheduled for Step 93.
*   This step focuses on the reliable delivery of the notification signal when the app is not in the foreground.
