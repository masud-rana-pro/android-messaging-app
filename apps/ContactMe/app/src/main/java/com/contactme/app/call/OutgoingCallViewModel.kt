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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.webrtc.PeerConnection
import javax.inject.Inject

@HiltViewModel
class OutgoingCallViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val callRepository: CallSignalingRepository,
    private val webRtcEngine: WebRtcCallEngine,
    private val okHttpClient: OkHttpClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(OutgoingCallUiState())
    val uiState: StateFlow<OutgoingCallUiState> = _uiState.asStateFlow()

    private var currentCallId: String? = null
    private var signalingJob: Job? = null
    private var iceCandidateJob: Job? = null
    private var timerJob: Job? = null
    private val processedIceCandidateIds = mutableSetOf<String>()

    fun startCall(receiverId: String, type: CallType) {
        if (receiverId.isBlank()) {
            Log.e(TAG, "startCall failed: receiverId is blank")
            return
        }
        if (currentCallId != null && uiState.value.status != CallStatus.Ended) {
            Log.w(TAG, "startCall: Call already in progress, ID: $currentCallId")
            return
        }
        val callerId = authRepository.currentUserId() ?: return
        Log.d(TAG, "Starting $type call to $receiverId from $callerId")
        
        viewModelScope.launch {
            val receiverProfile = profileRepository.getProfile(receiverId)
            Log.d(TAG, "Resolved receiver profile: ${receiverProfile?.displayName}, FCM: ${receiverProfile?.userId}")
            // Note: receiverProfile doesn't have fcmToken in our UserProfile model, but it is in Firestore.
            // We should ensure the receiver has a token.
            _uiState.update { it.copy(status = CallStatus.Ringing, receiverId = receiverId, receiverProfile = receiverProfile) }
        }

        webRtcEngine.initialize(callerId, object : WebRtcCallEngine.Listener {
            override fun onLocalDescription(sdp: String) {
                Log.d(TAG, "Local description created, sending offer...")
                viewModelScope.launch {
                    val result = callRepository.createCallOffer(callerId, receiverId, type, sdp)
                    if (result is CallResult.Created) {
                        Log.d(TAG, "Call offer created in Firestore: ${result.callId}")
                        currentCallId = result.callId
                        CallForegroundService.start(context, result.callId)
                        triggerCallNotification(result.callId, receiverId)
                        observeCall(result.callId)
                        observeIceCandidates(result.callId)
                    } else if (result is CallResult.Error) {
                        Log.e(TAG, "Failed to create call offer: ${result.message}")
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
                Log.d(TAG, "PeerConnection state changed: $state")
                _uiState.update { 
                    it.copy(connectionState = state)
                }
                if (state == PeerConnection.PeerConnectionState.CONNECTED) {
                    startTimer()
                    currentCallId?.let { id ->
                        viewModelScope.launch {
                            callRepository.updateCallStatus(id, CallStatus.Connected)
                        }
                    }
                } else if (state == PeerConnection.PeerConnectionState.CONNECTING) {
                    currentCallId?.let { id ->
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

        webRtcEngine.createOffer()
    }

    private fun observeCall(callId: String) {
        signalingJob?.cancel()
        signalingJob = viewModelScope.launch {
            callRepository.listenToCall(callId).collectLatest { session ->
                if (session == null) {
                    Log.d(TAG, "Call session is null, ending call")
                    _uiState.update { it.copy(status = CallStatus.Ended) }
                    return@collectLatest
                }

                Log.d(TAG, "Call status updated: ${session.status}")
                _uiState.update { it.copy(status = session.status) }

                if (session.status == CallStatus.Accepted && session.answer.isNotBlank()) {
                    Log.d(TAG, "Call accepted, setting remote description")
                    webRtcEngine.setRemoteDescription(session.answer)
                }

                if (session.status == CallStatus.Rejected || 
                    session.status == CallStatus.Ended || 
                    session.status == CallStatus.Cancelled ||
                    session.status == CallStatus.Busy ||
                    session.status == CallStatus.Timeout) {
                    Log.d(TAG, "Call reached terminal status: ${session.status}, cleaning up")
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
                        Log.e(TAG, "Failed to trigger call notification: ${response.code}")
                    } else {
                        Log.d(TAG, "Call notification triggered successfully")
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

    fun endCall() {
        val callId = currentCallId ?: return
        val userId = authRepository.currentUserId() ?: return
        Log.d(TAG, "Ending call: $callId")
        viewModelScope.launch {
            callRepository.endCall(callId, userId)
            cleanup()
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

    private fun cleanup() {
        Log.d(TAG, "Cleaning up outgoing call")
        CallForegroundService.stop(context)
        webRtcEngine.release()
        signalingJob?.cancel()
        iceCandidateJob?.cancel()
        timerJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }

    private companion object {
        const val TAG = "OutgoingCallViewModel"
        const val WORKER_URL = "https://contactme-call-notification.masud-jee68.workers.dev"
    }
}

data class OutgoingCallUiState(
    val status: CallStatus = CallStatus.Ringing,
    val receiverId: String = "",
    val receiverProfile: UserProfile? = null,
    val isMuted: Boolean = false,
    val isSpeakerEnabled: Boolean = false,
    val connectionState: PeerConnection.PeerConnectionState = PeerConnection.PeerConnectionState.NEW,
    val durationSeconds: Long = 0L
)
