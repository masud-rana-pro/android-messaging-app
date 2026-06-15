package com.contactme.app.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {
    override suspend fun submitAuth(
        emailOrPhone: String,
        password: String
    ): AuthResult {
        if (emailOrPhone.isBlank()) {
            return AuthResult.Error("Email is required.")
        }

        if (password.length < 6) {
            return AuthResult.Error("Password must be at least 6 characters.")
        }

        return runCatching {
            firebaseAuth.signInWithEmailAndPassword(
                emailOrPhone.trim(),
                password
            ).await()
        }.fold(
            onSuccess = { AuthResult.Success },
            onFailure = { error ->
                AuthResult.Error(
                    error.message ?: "Authentication failed. Please try again."
                )
            }
        )
    }
}
