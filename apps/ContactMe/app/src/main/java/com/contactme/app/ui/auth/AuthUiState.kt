package com.contactme.app.ui.auth

import com.contactme.app.navigation.AuthMode

data class AuthUiState(
    val authMode: AuthMode = AuthMode.Phone,
    val phoneNumber: String = "",
    val otpCode: String = "",
    val phoneVerificationId: String? = null,
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null
) {
    val isOtpSent: Boolean = phoneVerificationId != null
}
