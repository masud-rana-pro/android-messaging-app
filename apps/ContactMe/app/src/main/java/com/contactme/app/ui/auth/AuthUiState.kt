package com.contactme.app.ui.auth

import com.contactme.app.navigation.AuthMode

data class AuthUiState(
    val authMode: AuthMode = AuthMode.Login,
    val emailOrPhone: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
