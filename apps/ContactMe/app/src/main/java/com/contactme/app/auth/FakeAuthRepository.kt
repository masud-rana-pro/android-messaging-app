package com.contactme.app.auth

import android.app.Activity
import kotlinx.coroutines.delay
import javax.inject.Inject

class FakeAuthRepository @Inject constructor() : AuthRepository {
    private var signedIn = false

    override fun hasActiveSession(): Boolean {
        return signedIn
    }

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

        val result = when {
            email.isBlank() -> AuthResult.Error("Email is required.")
            !email.contains("@") -> AuthResult.Error("Enter a valid email address.")
            password.length < 6 -> AuthResult.Error("Password must be at least 6 characters.")
            else -> AuthResult.Success
        }

        if (result == AuthResult.Success) {
            signedIn = true
        }

        return result
    }

    override suspend fun requestPhoneOtp(
        phoneNumber: String,
        activity: Activity
    ): PhoneOtpResult {
        delay(350)
        val normalizedPhoneNumber = PhoneNumberFormatter.normalizeBangladeshNumber(phoneNumber)

        return when {
            phoneNumber.isBlank() -> PhoneOtpResult.Error("Phone number is required.")
            normalizedPhoneNumber == null -> PhoneOtpResult.Error("Enter a valid Bangladesh phone number.")
            else -> PhoneOtpResult.CodeSent("fake-verification-id")
        }
    }

    override suspend fun verifyPhoneOtp(
        verificationId: String,
        otpCode: String
    ): AuthResult {
        delay(350)

        val result = when {
            verificationId.isBlank() -> AuthResult.Error("Verification session expired. Request a new code.")
            otpCode.length != 6 -> AuthResult.Error("Enter the 6-digit verification code.")
            else -> AuthResult.Success
        }

        if (result == AuthResult.Success) {
            signedIn = true
        }

        return result
    }

    override fun signOut() {
        signedIn = false
    }
}
