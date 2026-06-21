package com.contactme.app.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contactme.app.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.webrtc.PeerConnection
import javax.inject.Inject

@HiltViewModel
class OutgoingCallViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val callRepository: CallSignalingRepository,
    private val webRtcEngine: WebRtcCallEngine,
    private val okHttpClient: OkHttpClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(OutgoingCallUiState())
    val uiState: StateFlow<OutgoingCallUiState> = _uiState.asStateFlow()

    private var currentCallId: String? = null
    private var signalingJob: Job? = null
    private var iceCandidateJob: Job? = null
    private val processedIceCandidateIds = mutableSetOf<String>()

    fun startCall(receiverId: String, type: CallType) {
        val callerId = authRepository.currentUserId() ?: return
        
        _uiState.update { it.copy(status = CallStatus.Ringing, receiverId = receiverId) }

        webRtcEngine.initialize(callerId, object : WebRtcCallEngine.Listener {
            override fun onLocalDescription(sdp: String) {
                viewModelScope.launch {
                    val result = callRepository.createCallOffer(callerId, receiverId, type, sdp)
                    if (result is CallResult.Created) {
                        currentCallId = result.callId
                        triggerCallNotification(result.callId, receiverId)
                        observeCall(result.callId)
                        observeIceCandidates(result.callId)
                    } else if (result is CallResult.Error) {
                        _uiState.update { it.copy(status = CallStatus.Ended) }
                    }
                }
            }

            override fun onIceCandidate(candidate: CallIceCandidate) {
                currentCallId?.let { callId ->
                    viewModelScope.launch {
                        callRepository.addCallerIceCandidate(callId, callerId, candidate)
                    }
                }
            }

            override fun onConnectionStateChange(state: PeerConnection.PeerConnectionState) {
                _uiState.update { 
                    it.copy(connectionState = state)
                }
            }
        })

        webRtcEngine.createOffer()
    }

    private fun observeCall(callId: String) {
        signalingJob?.cancel()
        signalingJob = viewModelScope.launch {
            callRepository.listenToCall(callId).collectLatest { session ->
                if (session == null) {
                    _uiState.update { it.copy(status = CallStatus.Ended) }
                    return@collectLatest
                }

                _uiState.update { it.copy(status = session.status) }

                if (session.status == CallStatus.Accepted && session.answer.isNotBlank()) {
                    webRtcEngine.setRemoteDescription(session.answer)
                }

                if (session.status == CallStatus.Rejected || 
                    session.status == CallStatus.Ended || 
                    session.status == CallStatus.Cancelled ||
                    session.status == CallStatus.Busy ||
                    session.status == CallStatus.Timeout) {
                    cleanup()
                }
            }
        }
    }

    private fun observeIceCandidates(callId: String) {
        iceCandidateJob?.cancel()
        iceCandidateJob = viewModelScope.launch {
            callRepository.listenReceiverIceCandidates(callId).collectLatest { candidates ->
                candidates.forEach { candidate ->
                    if (candidate.id.isNotBlank() && !processedIceCandidateIds.contains(candidate.id)) {
                        webRtcEngine.addIceCandidate(candidate)
                        processedIceCandidateIds.add(candidate.id)
                    }
                }
            }
        }
    }

    private fun triggerCallNotification(callId: String, receiverId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val payload = JSONObject().apply {
                    put("callId", callId)
                    put("receiverId", receiverId)
                }.toString()

                val request = Request.Builder()
                    .url(WORKER_URL)
                    .post(payload.toRequestBody("application/json".toMediaType()))
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        // Log failure safely (Step 92)
                    }
                }
            }
        }
    }

    fun cancelCall() {
        val callId = currentCallId ?: return
        val userId = authRepository.currentUserId() ?: return
        viewModelScope.launch {
            callRepository.endCall(callId, userId)
            cleanup()
        }
    }

    private fun cleanup() {
        webRtcEngine.release()
        signalingJob?.cancel()
        iceCandidateJob?.cancel()
        // We don't necessarily want to set status to ENDED if it's already REJECTED/CANCELLED in UI state
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }

    private companion object {
        const val WORKER_URL = "https://contactme-call-notification.masud-jee68.workers.dev"
    }
}

data class OutgoingCallUiState(
    val status: CallStatus = CallStatus.Ringing,
    val receiverId: String = "",
    val connectionState: PeerConnection.PeerConnectionState = PeerConnection.PeerConnectionState.NEW
)
