package com.contactme.app.auth

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
}
