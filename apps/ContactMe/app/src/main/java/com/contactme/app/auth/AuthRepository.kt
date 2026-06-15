package com.contactme.app.auth

interface AuthRepository {
    suspend fun signIn(
        email: String,
        password: String
    ): AuthResult

    suspend fun register(
        email: String,
        password: String
    ): AuthResult
}
