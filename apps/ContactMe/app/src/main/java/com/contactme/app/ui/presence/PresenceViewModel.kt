package com.contactme.app.ui.presence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contactme.app.auth.AuthRepository
import com.contactme.app.presence.PresenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class PresenceViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val presenceRepository: PresenceRepository
) : ViewModel() {
    fun markOnline() {
        val userId = authRepository.currentUserId() ?: return

        viewModelScope.launch {
            presenceRepository.markOnline(userId)
        }
    }

    fun markOffline() {
        val userId = authRepository.currentUserId() ?: return

        viewModelScope.launch {
            presenceRepository.markOffline(userId)
        }
    }
}
