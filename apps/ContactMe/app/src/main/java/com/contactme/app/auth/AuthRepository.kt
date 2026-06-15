package com.contactme.app.auth

import android.app.Activity

interface AuthRepository {
    suspend fun signIn(
        email: String,
        password: String
    ): AuthResult

    suspend fun register(
        email: String,
        password: String
    ): AuthResult

    suspend fun requestPhoneOtp(
        phoneNumber: String,
        activity: Activity
    ): PhoneOtpResult

    suspend fun verifyPhoneOtp(
        verificationId: String,
        otpCode: String
    ): AuthResult
}
