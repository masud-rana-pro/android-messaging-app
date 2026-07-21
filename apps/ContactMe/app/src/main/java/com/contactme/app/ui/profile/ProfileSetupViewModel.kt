package com.contactme.app.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contactme.app.auth.AuthRepository
import com.contactme.app.profile.ProfilePhotoRepository
import com.contactme.app.profile.ProfilePhotoResult
import com.contactme.app.profile.ProfileRepository
import com.contactme.app.profile.ProfileResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val profilePhotoRepository: ProfilePhotoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileSetupUiState())
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()

    init {
        loadExistingProfile()
    }

    fun onDisplayNameChanged(value: String) {
        _uiState.update {
            it.copy(
                displayName = value.take(MAX_DISPLAY_NAME_LENGTH),
                errorMessage = null
            )
        }
    }

    fun onUsernameChanged(value: String) {
        val normalizedUsername = value
            .lowercase()
            .filter { character ->
                character.isLetterOrDigit() || character == '_' || character == '.'
            }
            .take(MAX_USERNAME_LENGTH)

        _uiState.update {
            it.copy(
                username = normalizedUsername,
                errorMessage = null
            )
        }
    }

    fun onPhotoSelected(uri: Uri?) {
        _uiState.update {
            it.copy(
                selectedPhotoUri = uri?.toString().orEmpty(),
                errorMessage = null
            )
        }
    }

    fun saveProfile(onProfileReady: () -> Unit) {
        val state = _uiState.value
        val userId = authRepository.currentUserId()

        if (state.isLoading) return

        val validationMessage = validateProfile(
            displayName = state.displayName,
            username = state.username,
            userId = userId,
            hasPhoto = state.selectedPhotoUri.isNotBlank() || state.photoUrl.isNotBlank()
        )

        if (validationMessage != null) {
            _uiState.update { it.copy(errorMessage = validationMessage) }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val photoUrl = uploadSelectedPhotoIfNeeded(
                userId = userId.orEmpty(),
                state = state
            ) ?: return@launch

            when (
                val result = profileRepository.saveProfile(
                    userId = userId.orEmpty(),
                    displayName = state.displayName,
                    username = state.username,
                    phoneNumber = authRepository.registrationPhoneNumber(),
                    photoUrl = photoUrl
                )
            ) {
                ProfileResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onProfileReady()
                }

                is ProfileResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    private fun loadExistingProfile() {
        val userId = authRepository.currentUserId() ?: return

        viewModelScope.launch {
            val profile = profileRepository.getProfile(userId) ?: return@launch

            _uiState.update {
                it.copy(
                    displayName = profile.displayName,
                    username = profile.username,
                    photoUrl = profile.photoUrl,
                    selectedPhotoUri = "",
                    isExistingProfile = true,
                    errorMessage = null
                )
            }
        }
    }

    private fun validateProfile(
        displayName: String,
        username: String,
        userId: String?,
        hasPhoto: Boolean
    ): String? {
        return when {
            userId == null -> "Session expired. Please log in again."
            displayName.trim().length < MIN_DISPLAY_NAME_LENGTH -> {
                "Enter your display name."
            }
            username.trim().length < MIN_USERNAME_LENGTH -> {
                "Username must be at least 3 characters."
            }
            !hasPhoto -> "Choose a profile photo to continue."
            else -> null
        }
    }

    private companion object {
        const val MIN_DISPLAY_NAME_LENGTH = 2
        const val MAX_DISPLAY_NAME_LENGTH = 40
        const val MIN_USERNAME_LENGTH = 3
        const val MAX_USERNAME_LENGTH = 24
    }

    private suspend fun uploadSelectedPhotoIfNeeded(
        userId: String,
        state: ProfileSetupUiState
    ): String? {
        if (state.selectedPhotoUri.isBlank()) {
            return state.photoUrl
        }

        return when (
            val result = profilePhotoRepository.uploadProfilePhoto(
                userId = userId,
                photoUri = Uri.parse(state.selectedPhotoUri)
            )
        ) {
            is ProfilePhotoResult.Success -> result.photoUrl
            is ProfilePhotoResult.Error -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                null
            }
        }
    }
}
