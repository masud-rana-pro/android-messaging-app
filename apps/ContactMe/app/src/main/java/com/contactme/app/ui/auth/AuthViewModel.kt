package com.contactme.app.ui.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contactme.app.auth.AuthRepository
import com.contactme.app.auth.AuthResult
import com.contactme.app.auth.PhoneOtpResult
import com.contactme.app.navigation.AuthMode
import com.contactme.app.profile.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onAuthModeChanged(authMode: AuthMode) {
        _uiState.update {
            it.copy(
                authMode = authMode,
                statusMessage = if (authMode != AuthMode.Phone) "For emulator testing, use Email registration." else null,
                errorMessage = null
            )
        }
    }

    fun onPhoneNumberChanged(value: String) {
        _uiState.update {
            it.copy(
                phoneNumber = value,
                statusMessage = null,
                errorMessage = null
            )
        }
    }

    fun onOtpCodeChanged(value: String) {
        _uiState.update {
            it.copy(
                otpCode = value.filter(Char::isDigit).take(6),
                statusMessage = null,
                errorMessage = null
            )
        }
    }

    fun onEmailChanged(value: String) {
        _uiState.update {
            it.copy(
                email = value,
                statusMessage = null,
                errorMessage = null
            )
        }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                statusMessage = null,
                errorMessage = null
            )
        }
    }

    fun submit(
        activity: Activity?,
        onSuccess: () -> Unit
    ) {
        val state = _uiState.value

        if (state.isLoading) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    statusMessage = null,
                    errorMessage = null
                )
            }

            if (state.authMode == AuthMode.Phone) {
                submitPhoneAuth(
                    state = state,
                    activity = activity,
                    onSuccess = onSuccess
                )
                return@launch
            }

            val result = when (state.authMode) {
                AuthMode.Phone -> AuthResult.Error("Invalid auth state.")
                AuthMode.EmailLogin -> authRepository.signIn(
                    email = state.email,
                    password = state.password
                )

                AuthMode.EmailRegister -> authRepository.register(
                    email = state.email,
                    phoneNumber = state.phoneNumber,
                    password = state.password
                )
            }

            when (result) {
                AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }

                is AuthResult.Error -> {
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

    fun resetPassword() {
        val email = _uiState.value.email
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, statusMessage = null) }
            when (val result = authRepository.sendPasswordReset(email)) {
                AuthResult.Success -> _uiState.update { it.copy(isLoading = false, statusMessage = "Password reset link sent. Check your email.") }
                is AuthResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    private suspend fun submitPhoneAuth(
        state: AuthUiState,
        activity: Activity?,
        onSuccess: () -> Unit
    ) {
        if (activity == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Could not start phone verification. Please try again."
                )
            }
            return
        }

        val verificationId = state.phoneVerificationId

        if (verificationId == null) {
            when (
                val result = authRepository.requestPhoneOtp(
                    phoneNumber = state.phoneNumber,
                    activity = activity
                )
            ) {
                is PhoneOtpResult.CodeSent -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            phoneVerificationId = result.verificationId,
                            statusMessage = "Code sent. Check your SMS messages.",
                            errorMessage = null
                        )
                    }
                }

                PhoneOtpResult.Verified -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }

                is PhoneOtpResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
            return
        }

        when (
            val result = authRepository.verifyPhoneOtp(
                verificationId = verificationId,
                otpCode = state.otpCode
            )
        ) {
            AuthResult.Success -> {
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            }

            is AuthResult.Error -> {
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
