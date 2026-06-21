# Step 93: Microphone, Speaker, Mute and Foreground Service

Implemented practical audio-call controls and a foreground service to maintain call stability.

## Key Improvements

### 1. CallForegroundService
- Keeps the active audio call alive when the app moves to the background or the screen is locked.
- Shows an ongoing "Call in progress" notification using the dedicated "Calls" channel.
- Uses `FOREGROUND_SERVICE_TYPE_PHONE_CALL` for Android 14+ compatibility.

### 2. Media Controls in WebRtcCallEngine
- `setMuted(Boolean)`: Toggles the local audio track.
- `setSpeakerEnabled(Boolean)`: Manages audio routing via `AudioManager.MODE_IN_COMMUNICATION`.

### 3. Orchestration in ViewModels
- `OutgoingCallViewModel` and `IncomingCallViewModel` now manage `isMuted` and `isSpeakerEnabled` states.
- Automatically start `CallForegroundService` when a call is established (or ringing starts).
- Stop the service and release WebRTC resources on call end, reject, or failure.

### 4. Runtime Permissions
- Integrated `RECORD_AUDIO` permission request flow in both `OutgoingCallScreen` and `IncomingCallScreen` using `rememberLauncherForActivityResult`.
- Prevents starting/accepting calls without the required permission.

### 5. UI Polish
- Added a modern `CallControlBar` with:
    - **Mute/Unmute**: Red background when muted.
    - **Speaker/Earpiece**: Primary color background when speaker is on.
    - **End Call**: Distinct red circular button.

## Verification
- Manifest updated with `FOREGROUND_SERVICE` permissions.
- Build successful with new service and engine controls.
- UI state reflects local media changes.
