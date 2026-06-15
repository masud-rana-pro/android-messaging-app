package com.contactme.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contactme.app.auth.AuthRepository
import com.contactme.app.auth.AuthResult
import com.contactme.app.navigation.AuthMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onAuthModeChanged(authMode: AuthMode) {
        _uiState.update {
            it.copy(
                authMode = authMode,
                errorMessage = null
            )
        }
    }

    fun onPhoneNumberChanged(value: String) {
        _uiState.update {
            it.copy(
                phoneNumber = value,
                errorMessage = null
            )
        }
    }

    fun onEmailChanged(value: String) {
        _uiState.update {
            it.copy(
                email = value,
                errorMessage = null
            )
        }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                errorMessage = null
            )
        }
    }

    fun submit(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.isLoading) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val result = when (state.authMode) {
                AuthMode.Phone -> requestPhoneOtp(state.phoneNumber)

                AuthMode.EmailLogin -> authRepository.signIn(
                    email = state.email,
                    password = state.password
                )

                AuthMode.EmailRegister -> authRepository.register(
                    email = state.email,
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

    private fun requestPhoneOtp(phoneNumber: String): AuthResult {
        return when {
            phoneNumber.isBlank() -> AuthResult.Error("Phone number is required.")
            phoneNumber.filter(Char::isDigit).length < 10 -> {
                AuthResult.Error("Enter a valid phone number.")
            }
            else -> AuthResult.Error("Phone OTP verification will be connected in the next auth step.")
        }
    }
}
