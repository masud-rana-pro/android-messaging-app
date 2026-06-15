package com.contactme.app.ui.auth

import com.contactme.app.navigation.AuthMode

data class AuthUiState(
    val authMode: AuthMode = AuthMode.Phone,
    val phoneNumber: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
