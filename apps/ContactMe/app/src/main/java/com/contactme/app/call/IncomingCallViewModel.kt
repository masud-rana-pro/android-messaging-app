package com.contactme.app.call

import android.util.Log
import android.media.Ringtone
import android.media.RingtoneManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contactme.app.auth.AuthRepository
import com.contactme.app.profile.ProfileRepository
import com.contactme.app.profile.UserProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import androidx.core.app.NotificationManagerCompat
import org.webrtc.PeerConnection
import javax.inject.Inject

import org.webrtc.VideoTrack
import org.webrtc.SurfaceViewRenderer

@HiltViewModel
class IncomingCallViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val callRepository: CallSignalingRepository,
    private val webRtcEngine: WebRtcCallEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(IncomingCallUiState())
    val uiState: StateFlow<IncomingCallUiState> = _uiState.asStateFlow()

    private var incomingCallsJob: Job? = null
    private var signalingJob: Job? = null
    private var iceCandidateJob: Job? = null
    private var timerJob: Job? = null
    private val processedIceCandidateIds = mutableSetOf<String>()
    private var isAcceptingCall = false
    private var ringtone: Ringtone? = null

    init {
        observeIncomingCalls()
    }

    private fun observeIncomingCalls() {
        val userId = authRepository.currentUserId() ?: return
        Log.d(TAG, "Observing incoming calls for $userId")
        incomingCallsJob?.cancel()
        incomingCallsJob = viewModelScope.launch {
            callRepository.listenForIncomingCalls(userId).collectLatest { calls ->
                Log.d(TAG, "Incoming calls update: count=${calls.size}")
                val now = System.currentTimeMillis()
                val ringingCall = calls.firstOrNull {
                    it.status == CallStatus.Ringing &&
                        it.createdAtMillis > 0L &&
                        now - it.createdAtMillis <= MAX_INCOMING_CALL_AGE_MILLIS
                }
                if (ringingCall != null && ringingCall.callId != _uiState.value.activeCall?.callId) {
                    Log.d(TAG, "New active incoming call detected: ${ringingCall.callId}")
                    val callerProfile = profileRepository.getProfile(ringingCall.callerId)
                    resetForIncomingCall(ringingCall, callerProfile)
                    startRinging()
                }
            }
        }
    }

    fun acceptCall() {
        val call = _uiState.value.activeCall ?: return
        val currentUserId = authRepository.currentUserId() ?: return
        if (isAcceptingCall || _uiState.value.status != CallStatus.Ringing) return
        isAcceptingCall = true
        timerJob?.cancel()
        timerJob = null
        stopRinging()
        cancelCallNotification(call.callId)
        _uiState.update {
            it.copy(
                status = CallStatus.Accepted,
                durationSeconds = 0L,
                connectionState = PeerConnection.PeerConnectionState.NEW,
                remoteVideoTrack = null
            )
        }
        Log.d(TAG, "Accepting call: ${call.callId}")

        CallForegroundService.start(
            context = context,
            callId = call.callId,
            callType = call.type,
            role = ActiveCallStore.ROLE_INCOMING,
            peerId = call.callerId
        )

        webRtcEngine.initialize(currentUserId, call.type, object : WebRtcCallEngine.Listener {
            override fun onLocalDescription(sdp: String) {
                Log.d(TAG, "Local description created (answer)")
                viewModelScope.launch {
                    callRepository.acceptCallWithAnswer(call.callId, currentUserId, sdp)
                }
            }

            override fun onIceCandidate(candidate: CallIceCandidate) {
                viewModelScope.launch {
                    val result = callRepository.addReceiverIceCandidate(call.callId, currentUserId, candidate)
                    if (result is CallResult.Error) {
                        Log.e(TAG, "Failed to publish receiver ICE candidate: ${result.message}")
                    }
                }
            }

            override fun onConnectionStateChange(state: PeerConnection.PeerConnectionState) {
                Log.d(TAG, "PeerConnection state changed: $state")
                _uiState.update { it.copy(connectionState = state) }
                if (state == PeerConnection.PeerConnectionState.CONNECTED) {
                    startTimer()
                    _uiState.value.activeCall?.callId?.let { id ->
                        viewModelScope.launch {
                            callRepository.updateCallStatus(id, CallStatus.Connected)
                        }
                    }
                } else if (state == PeerConnection.PeerConnectionState.CONNECTING) {
                    _uiState.value.activeCall?.callId?.let { id ->
                        viewModelScope.launch {
                            callRepository.updateCallStatus(id, CallStatus.Connecting)
                        }
                    }
                }
            }

            override fun onAudioTrackAdded() {
                Log.d(TAG, "Remote audio track added")
            }

            override fun onVideoTrackAdded(track: VideoTrack) {
                Log.d(TAG, "Remote video track added")
                _uiState.update { it.copy(remoteVideoTrack = track) }
            }
        })

        webRtcEngine.setRemoteOffer(call.offer) {
            webRtcEngine.createAnswer()
        }
        observeCall(call.callId)
        observeIceCandidates(call.callId)
    }

    fun rejectCall() {
        val call = _uiState.value.activeCall ?: return
        val currentUserId = authRepository.currentUserId() ?: return
        Log.d(TAG, "Rejecting call: ${call.callId}")
        stopRinging()
        cancelCallNotification(call.callId)
        viewModelScope.launch {
            callRepository.rejectCall(call.callId, currentUserId)
            clearActiveCall()
        }
    }

    fun endCall() {
        val call = _uiState.value.activeCall ?: return
        val currentUserId = authRepository.currentUserId() ?: return
        Log.d(TAG, "Ending call: ${call.callId}")
        cancelCallNotification(call.callId)
        viewModelScope.launch {
            callRepository.endCall(call.callId, currentUserId)
            _uiState.update { it.copy(status = CallStatus.Ended) }
            clearActiveCall()
        }
    }

    fun toggleMute() {
        val newState = !uiState.value.isMuted
        webRtcEngine.setMuted(newState)
        _uiState.update { it.copy(isMuted = newState) }
    }

    fun toggleSpeaker() {
        val newState = !uiState.value.isSpeakerEnabled
        webRtcEngine.setSpeakerEnabled(newState)
        _uiState.update { it.copy(isSpeakerEnabled = newState) }
    }

    fun setupLocalVideo(renderer: SurfaceViewRenderer) {
        webRtcEngine.setupLocalVideo(renderer)
    }

    fun setupRemoteVideo(renderer: SurfaceViewRenderer) {
        val track = uiState.value.remoteVideoTrack ?: return
        webRtcEngine.setupRemoteVideo(track, renderer)
    }

    fun switchCamera() {
        webRtcEngine.switchCamera()
    }

    fun openCallFromNotification(callId: String, onOpened: () -> Unit) {
        if (callId.isBlank()) return
        val currentUserId = authRepository.currentUserId() ?: return
        val current = _uiState.value.activeCall
        if (current?.callId == callId) {
            onOpened()
            return
        }
        viewModelScope.launch {
            val call = withTimeoutOrNull(NOTIFICATION_CALL_LOAD_TIMEOUT_MILLIS) {
                callRepository.listenToCall(callId).first { it != null }
            } ?: return@launch
            if (call.receiverId != currentUserId || call.status !in RESTORABLE_CALL_STATUSES) return@launch
            val callerProfile = profileRepository.getProfile(call.callerId)
            _uiState.update { it.copy(activeCall = call, status = call.status, callerProfile = callerProfile) }
            if (call.status == CallStatus.Ringing) startRinging()
            onOpened()
        }
    }

    private fun observeCall(callId: String) {
        signalingJob?.cancel()
        signalingJob = viewModelScope.launch {
            callRepository.listenToCall(callId).collectLatest { session ->
                if (session == null) {
                    Log.d(TAG, "Call session is null, clearing active call")
                    clearActiveCall()
                    return@collectLatest
                }

                Log.d(TAG, "Call status updated: ${session.status}")
                _uiState.update { it.copy(status = session.status) }

                if (session.status == CallStatus.Rejected ||
                    session.status == CallStatus.Ended ||
                    session.status == CallStatus.Cancelled ||
                    session.status == CallStatus.Busy ||
                    session.status == CallStatus.Timeout) {
                    Log.d(TAG, "Call reached terminal status: ${session.status}, clearing active call")
                    clearActiveCall()
                }
            }
        }
    }

    private fun observeIceCandidates(callId: String) {
        iceCandidateJob?.cancel()
        iceCandidateJob = viewModelScope.launch {
            callRepository.listenCallerIceCandidates(callId).collectLatest { candidates ->
                candidates.forEach { candidate ->
                    if (candidate.id.isNotBlank() && !processedIceCandidateIds.contains(candidate.id)) {
                        webRtcEngine.addIceCandidate(candidate)
                        processedIceCandidateIds.add(candidate.id)
                    }
                }
            }
        }
    }

    private fun startTimer() {
        if (timerJob != null) return
        timerJob = viewModelScope.launch {
            var seconds = 0L
            while (true) {
                delay(1000)
                seconds++
                _uiState.update { it.copy(durationSeconds = seconds) }
            }
        }
    }

    private fun clearActiveCall() {
        Log.d(TAG, "Clearing active call")
        stopRinging()
        CallForegroundService.stop(context)
        ActiveCallStore.clear(context, _uiState.value.activeCall?.callId)
        webRtcEngine.release()
        signalingJob?.cancel()
        iceCandidateJob?.cancel()
        timerJob?.cancel()
        processedIceCandidateIds.clear()
        isAcceptingCall = false
        _uiState.update {
            it.copy(
                activeCall = null,
                callerProfile = null,
                status = CallStatus.Ended,
                isMuted = false,
                isSpeakerEnabled = false,
                connectionState = PeerConnection.PeerConnectionState.CLOSED,
                durationSeconds = 0L,
                remoteVideoTrack = null
            )
        }
    }

    fun onCallScreenDismissed() {
        _uiState.update { it.copy(activeCall = null, durationSeconds = 0L, remoteVideoTrack = null) }
    }

    private fun resetForIncomingCall(call: CallSession, callerProfile: UserProfile?) {
        signalingJob?.cancel()
        iceCandidateJob?.cancel()
        timerJob?.cancel()
        timerJob = null
        webRtcEngine.release()
        processedIceCandidateIds.clear()
        isAcceptingCall = false
        _uiState.value = IncomingCallUiState(
            activeCall = call,
            callerProfile = callerProfile,
            status = CallStatus.Ringing,
            connectionState = PeerConnection.PeerConnectionState.NEW
        )
    }

    override fun onCleared() {
        super.onCleared()
        stopRinging()
        webRtcEngine.release()
    }

    private fun cancelCallNotification(callId: String) {
        NotificationManagerCompat.from(context).cancel(callId.hashCode() and Int.MAX_VALUE)
    }

    private fun startRinging() {
        if (ringtone?.isPlaying == true) return
        ringtone = runCatching {
            RingtoneManager.getRingtone(
                context,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            )?.apply {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) isLooping = true
                play()
            }
        }.getOrNull()
    }

    private fun stopRinging() {
        runCatching { ringtone?.stop() }
        ringtone = null
    }

    private companion object {
        const val TAG = "IncomingCallViewModel"
        private const val NOTIFICATION_CALL_LOAD_TIMEOUT_MILLIS = 5_000L
        private val RESTORABLE_CALL_STATUSES = setOf(
            CallStatus.Ringing,
            CallStatus.Accepted,
            CallStatus.Connecting,
            CallStatus.Connected
        )
        const val MAX_INCOMING_CALL_AGE_MILLIS = 90_000L
    }
}

data class IncomingCallUiState(
    val activeCall: CallSession? = null,
    val callerProfile: UserProfile? = null,
    val status: CallStatus = CallStatus.Ended,
    val isMuted: Boolean = false,
    val isSpeakerEnabled: Boolean = false,
    val connectionState: PeerConnection.PeerConnectionState = PeerConnection.PeerConnectionState.NEW,
    val durationSeconds: Long = 0L,
    val remoteVideoTrack: VideoTrack? = null
)
