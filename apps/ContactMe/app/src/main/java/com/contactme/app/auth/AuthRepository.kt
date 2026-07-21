package com.contactme.app.auth

import android.app.Activity

interface AuthRepository {
    fun hasActiveSession(): Boolean

    fun currentUserId(): String?

    suspend fun signIn(
        email: String,
        password: String
    ): AuthResult

    suspend fun register(
        email: String,
        phoneNumber: String,
        password: String
    ): AuthResult

    suspend fun sendPasswordReset(email: String): AuthResult

    fun registrationPhoneNumber(): String

    suspend fun requestPhoneOtp(
        phoneNumber: String,
        activity: Activity
    ): PhoneOtpResult

    suspend fun verifyPhoneOtp(
        verificationId: String,
        otpCode: String
    ): AuthResult

    fun signOut()
}
