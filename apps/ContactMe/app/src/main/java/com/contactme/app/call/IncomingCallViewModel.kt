package com.contactme.app.call

import android.util.Log
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
import org.webrtc.PeerConnection
import javax.inject.Inject

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

    init {
        observeIncomingCalls()
    }

    private fun observeIncomingCalls() {
        val userId = authRepository.currentUserId() ?: return
        Log.d(TAG, "Observing incoming calls for $userId")
        incomingCallsJob?.cancel()
        incomingCallsJob = viewModelScope.launch {
            callRepository.listenForIncomingCalls(userId).collectLatest { calls ->
                val ringingCall = calls.firstOrNull { it.status == CallStatus.Ringing && it.type == CallType.Audio }
                if (ringingCall != null && ringingCall.callId != _uiState.value.activeCall?.callId) {
                    Log.d(TAG, "New incoming call detected: ${ringingCall.callId}")
                    val callerProfile = profileRepository.getProfile(ringingCall.callerId)
                    _uiState.update { it.copy(activeCall = ringingCall, status = CallStatus.Ringing, callerProfile = callerProfile) }
                }
            }
        }
    }

    fun acceptCall() {
        val call = _uiState.value.activeCall ?: return
        val currentUserId = authRepository.currentUserId() ?: return
        Log.d(TAG, "Accepting call: ${call.callId}")

        CallForegroundService.start(context, call.callId)

        webRtcEngine.initialize(currentUserId, object : WebRtcCallEngine.Listener {
            override fun onLocalDescription(sdp: String) {
                Log.d(TAG, "Local description created (answer)")
                viewModelScope.launch {
                    callRepository.acceptCallWithAnswer(call.callId, currentUserId, sdp)
                }
            }

            override fun onIceCandidate(candidate: CallIceCandidate) {
                viewModelScope.launch {
                    callRepository.addReceiverIceCandidate(call.callId, currentUserId, candidate)
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
        })

        webRtcEngine.setRemoteOffer(call.offer)
        webRtcEngine.createAnswer()
        observeCall(call.callId)
        observeIceCandidates(call.callId)
    }

    fun rejectCall() {
        val call = _uiState.value.activeCall ?: return
        val currentUserId = authRepository.currentUserId() ?: return
        Log.d(TAG, "Rejecting call: ${call.callId}")
        viewModelScope.launch {
            callRepository.rejectCall(call.callId, currentUserId)
            clearActiveCall()
        }
    }

    fun endCall() {
        val call = _uiState.value.activeCall ?: return
        val currentUserId = authRepository.currentUserId() ?: return
        Log.d(TAG, "Ending call: ${call.callId}")
        viewModelScope.launch {
            callRepository.endCall(call.callId, currentUserId)
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
        CallForegroundService.stop(context)
        webRtcEngine.release()
        signalingJob?.cancel()
        iceCandidateJob?.cancel()
        timerJob?.cancel()
        processedIceCandidateIds.clear()
        _uiState.update { it.copy(activeCall = null, status = CallStatus.Ended) }
    }

    fun onCallScreenDismissed() {
        _uiState.update { it.copy(activeCall = null) }
    }

    override fun onCleared() {
        super.onCleared()
        webRtcEngine.release()
    }

    private companion object {
        const val TAG = "IncomingCallViewModel"
    }
}

data class IncomingCallUiState(
    val activeCall: CallSession? = null,
    val callerProfile: UserProfile? = null,
    val status: CallStatus = CallStatus.Ended,
    val isMuted: Boolean = false,
    val isSpeakerEnabled: Boolean = false,
    val connectionState: PeerConnection.PeerConnectionState = PeerConnection.PeerConnectionState.NEW,
    val durationSeconds: Long = 0L
)
