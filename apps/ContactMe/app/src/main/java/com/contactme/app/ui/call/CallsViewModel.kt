package com.contactme.app.ui.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contactme.app.auth.AuthRepository
import com.contactme.app.call.CallSignalingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CallsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val callRepository: CallSignalingRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CallListUiState())
    val uiState: StateFlow<CallListUiState> = _uiState.asStateFlow()

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
                _uiState.update {
                    it.copy(
                        calls = calls,
                        isLoading = false,
                        message = if (calls.isEmpty()) "No calls yet. Open a chat and tap Call." else null
                    )
                }
            }
        }
    }
}
