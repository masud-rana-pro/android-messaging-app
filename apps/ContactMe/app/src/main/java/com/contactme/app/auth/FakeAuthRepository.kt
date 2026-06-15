package com.contactme.app.auth

import android.app.Activity
import kotlinx.coroutines.delay
import javax.inject.Inject

class FakeAuthRepository @Inject constructor() : AuthRepository {
    override suspend fun signIn(
        email: String,
        password: String
    ): AuthResult = validateFakeAuth(
        email = email,
        password = password
    )

    override suspend fun register(
        email: String,
        password: String
    ): AuthResult = validateFakeAuth(
        email = email,
        password = password
    )

    private suspend fun validateFakeAuth(
        email: String,
        password: String
    ): AuthResult {
        delay(350)

        return when {
            email.isBlank() -> AuthResult.Error("Email is required.")
            !email.contains("@") -> AuthResult.Error("Enter a valid email address.")
            password.length < 6 -> AuthResult.Error("Password must be at least 6 characters.")
            else -> AuthResult.Success
        }
    }

    override suspend fun requestPhoneOtp(
        phoneNumber: String,
        activity: Activity
    ): PhoneOtpResult {
        delay(350)

        return when {
            phoneNumber.isBlank() -> PhoneOtpResult.Error("Phone number is required.")
            phoneNumber.filter(Char::isDigit).length < 10 -> {
                PhoneOtpResult.Error("Enter a valid phone number.")
            }
            else -> PhoneOtpResult.CodeSent("fake-verification-id")
        }
    }

    override suspend fun verifyPhoneOtp(
        verificationId: String,
        otpCode: String
    ): AuthResult {
        delay(350)

        return when {
            verificationId.isBlank() -> AuthResult.Error("OTP session expired. Send a new code.")
            otpCode.length != 6 -> AuthResult.Error("Enter the 6-digit OTP code.")
            else -> AuthResult.Success
        }
    }
}
