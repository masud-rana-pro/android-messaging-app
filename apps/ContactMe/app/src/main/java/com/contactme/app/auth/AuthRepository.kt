package com.contactme.app.auth

interface AuthRepository {
    suspend fun submitAuth(
        emailOrPhone: String,
        password: String
    ): AuthResult
}
