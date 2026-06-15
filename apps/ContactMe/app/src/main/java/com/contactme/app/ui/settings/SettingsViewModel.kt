package com.contactme.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contactme.app.auth.AuthRepository
import com.contactme.app.profile.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        val userId = authRepository.currentUserId()

        if (userId == null) {
            _uiState.update {
                it.copy(
                    isLoadingProfile = false,
                    errorMessage = "Session expired. Please log in again."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingProfile = true,
                    errorMessage = null
                )
            }

            val profile = profileRepository.getProfile(userId)

            _uiState.update {
                if (profile == null) {
                    it.copy(
                        isLoadingProfile = false,
                        errorMessage = "Profile details are not available yet."
                    )
                } else {
                    it.copy(
                        displayName = profile.displayName.ifBlank { "ContactMe User" },
                        username = profile.username.ifBlank { "contactme" },
                        isLoadingProfile = false,
                        errorMessage = null
                    )
                }
            }
        }
    }
}
