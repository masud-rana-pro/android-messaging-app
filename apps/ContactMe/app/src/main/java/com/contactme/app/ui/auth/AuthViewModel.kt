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

    fun onEmailOrPhoneChanged(value: String) {
        _uiState.update {
            it.copy(
                emailOrPhone = value,
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
                AuthMode.Login -> authRepository.signIn(
                    email = state.emailOrPhone,
                    password = state.password
                )

                AuthMode.Register -> authRepository.register(
                    email = state.emailOrPhone,
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
}
