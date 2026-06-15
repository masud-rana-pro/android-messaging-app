package com.contactme.app.auth

import kotlinx.coroutines.delay
import javax.inject.Inject

class FakeAuthRepository @Inject constructor() : AuthRepository {
    override suspend fun submitAuth(
        emailOrPhone: String,
        password: String
    ): AuthResult {
        delay(350)

        return when {
            emailOrPhone.isBlank() -> AuthResult.Error("Email or phone is required.")
            password.length < 6 -> AuthResult.Error("Password must be at least 6 characters.")
            else -> AuthResult.Success
        }
    }
}
