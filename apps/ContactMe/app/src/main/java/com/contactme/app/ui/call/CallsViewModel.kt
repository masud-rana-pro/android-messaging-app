package com.contactme.app.ui.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contactme.app.auth.AuthRepository
import com.contactme.app.call.CallSignalingRepository
import com.contactme.app.call.CallSession
import com.contactme.app.profile.ProfileRepository
import com.contactme.app.profile.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

@HiltViewModel
class CallsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val callRepository: CallSignalingRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CallListUiState())
    val uiState: StateFlow<CallListUiState> = _uiState.asStateFlow()

    private val profileCache = ConcurrentHashMap<String, UserProfile>()

    init {
        observeCalls()
    }

    private fun observeCalls() {
        val currentUserId = authRepository.currentUserId()
        if (currentUserId == null) {
            _uiState.update { it.copy(isLoading = false, message = "Session expired.") }
            return
        }

        _uiState.update { it.copy(currentUserId = currentUserId) }

        viewModelScope.launch {
            callRepository.listenToAllCalls(currentUserId).collect { calls ->
                resolveProfiles(calls, currentUserId)
                _uiState.update {
                    it.copy(
                        calls = calls,
                        profiles = profileCache.toMap(),
                        isLoading = false,
                        message = if (calls.isEmpty()) "No calls yet. Open a chat and tap Call." else null
                    )
                }
            }
        }
    }

    private suspend fun resolveProfiles(calls: List<CallSession>, currentUserId: String) {
        val peerIds = calls.map { if (it.callerId == currentUserId) it.receiverId else it.callerId }
            .filter { it.isNotBlank() && !profileCache.containsKey(it) }
            .distinct()

        peerIds.forEach { id ->
            runCatching {
                profileRepository.getProfile(id)
            }.getOrNull()?.let { profile ->
                profileCache[id] = profile
            }
        }
    }
}
