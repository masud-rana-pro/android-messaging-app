package com.contactme.app.auth

sealed interface PhoneOtpResult {
    data class CodeSent(val verificationId: String) : PhoneOtpResult
    data object Verified : PhoneOtpResult
    data class Error(val message: String) : PhoneOtpResult
}
