# Audio Call UI and Reliability

This document describes the improvements made to the audio calling feature, focusing on production-grade UI and connection reliability.

## Improvements

### 1. Production-Grade UI
-   **Outgoing Call Screen:** Replaced the debug screen with a polished interface showing the receiver's name, profile photo, and phone number (if available).
-   **Incoming Call Screen:** A new user-friendly incoming call UI with "Accept" and "Reject" buttons, showing the caller's profile.
-   **Active Call UI:** Shows call duration, status (Connected/Connecting), and controls for Mute, Speaker, and End Call.
-   **Calls Tab:** Redesigned to show recent calls with avatars, status icons (Incoming, Outgoing, Missed), and timestamps.

### 2. Reliability Fixes
-   **Permission Handling:** `RECORD_AUDIO` permission is checked/requested before starting or accepting a call.
-   **Connection Lifecycle:** Improved synchronization between WebRTC states and Firestore signaling. Added `Connecting` and `Connected` statuses to track real-time progress.
-   **Audio Routing:** Fixed speakerphone toggling and ensured local audio track is properly enabled/disabled during mute.
-   **Profile Resolution:** ViewModels now resolve peer user profiles (name, photo) so IDs are never shown to the user.
-   **Timer:** A real-time duration timer starts automatically once the call is connected.

### 3. Signaling Improvements
-   **Terminal Statuses:** The `endCall` logic now intelligently distinguishes between `Cancelled` (by caller) and `Rejected` (by receiver) based on the current state.
-   **ICE Candidates:** Improved logging for better debugging of NAT traversal issues.

## Real Phone Testing
The implementation supports WiFi-to-WiFi, WiFi-to-Mobile Data, and Mobile Data-to-Mobile Data calls using STUN/TURN servers configured via `BuildConfig`.

### RETEST FLOW

#### Outgoing Call
1. Open a chat with a user.
2. Tap the **Call** icon in the header.
3. Observe the polished "Calling..." screen with the user's name and photo.
4. Verify you can toggle **Mute** and **Speaker**.

#### Incoming Call
1. Receive a call while the app is in foreground or background.
2. Observe the "Incoming voice call" screen.
3. Tap **Accept** (grant permission if prompted).
4. Verify audio is heard on both sides and the timer starts.
5. Tap **End Call** and verify both sides return to the previous screen.

#### Calls Tab
1. Navigate to the **Calls** tab.
2. Verify the call history shows the correct direction and status.
3. Verify missed calls are highlighted (Red name/icon).
