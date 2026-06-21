package com.contactme.app.call

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contactme.app.auth.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.webrtc.PeerConnection
import javax.inject.Inject

@HiltViewModel
class IncomingCallViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val callRepository: CallSignalingRepository,
    private val webRtcEngine: WebRtcCallEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(IncomingCallUiState())
    val uiState: StateFlow<IncomingCallUiState> = _uiState.asStateFlow()

    private var incomingCallsJob: Job? = null
    private var signalingJob: Job? = null
    private var iceCandidateJob: Job? = null
    private val processedIceCandidateIds = mutableSetOf<String>()

    init {
        observeIncomingCalls()
    }

    private fun observeIncomingCalls() {
        val userId = authRepository.currentUserId() ?: return
        incomingCallsJob?.cancel()
        incomingCallsJob = viewModelScope.launch {
            callRepository.listenForIncomingCalls(userId).collectLatest { calls ->
                val ringingCall = calls.firstOrNull { it.status == CallStatus.Ringing && it.type == CallType.Audio }
                if (ringingCall != null && ringingCall.callId != _uiState.value.activeCall?.callId) {
                    _uiState.update { it.copy(activeCall = ringingCall, status = CallStatus.Ringing) }
                }
            }
        }
    }

    fun acceptCall() {
        val call = _uiState.value.activeCall ?: return
        val currentUserId = authRepository.currentUserId() ?: return

        CallForegroundService.start(context, call.callId)

        webRtcEngine.initialize(currentUserId, object : WebRtcCallEngine.Listener {
            override fun onLocalDescription(sdp: String) {
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
                _uiState.update { it.copy(connectionState = state) }
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
        viewModelScope.launch {
            callRepository.rejectCall(call.callId, currentUserId)
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
                    clearActiveCall()
                    return@collectLatest
                }

                _uiState.update { it.copy(status = session.status) }

                if (session.status == CallStatus.Rejected ||
                    session.status == CallStatus.Ended ||
                    session.status == CallStatus.Cancelled ||
                    session.status == CallStatus.Busy ||
                    session.status == CallStatus.Timeout) {
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

    private fun clearActiveCall() {
        CallForegroundService.stop(context)
        webRtcEngine.release()
        signalingJob?.cancel()
        iceCandidateJob?.cancel()
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
}

data class IncomingCallUiState(
    val activeCall: CallSession? = null,
    val status: CallStatus = CallStatus.Ended,
    val isMuted: Boolean = false,
    val isSpeakerEnabled: Boolean = false,
    val connectionState: PeerConnection.PeerConnectionState = PeerConnection.PeerConnectionState.NEW
)
