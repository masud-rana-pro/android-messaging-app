package com.contactme.app.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {
    override suspend fun signIn(
        email: String,
        password: String
    ): AuthResult = submitFirebaseAuth(
        email = email,
        password = password,
        operation = {
            firebaseAuth.signInWithEmailAndPassword(
                email.trim(),
                password
            ).await()
        }
    )

    override suspend fun register(
        email: String,
        password: String
    ): AuthResult = submitFirebaseAuth(
        email = email,
        password = password,
        operation = {
            firebaseAuth.createUserWithEmailAndPassword(
                email.trim(),
                password
            ).await()
        }
    )

    private suspend fun submitFirebaseAuth(
        email: String,
        password: String,
        operation: suspend () -> Any
    ): AuthResult {
        if (email.isBlank()) {
            return AuthResult.Error("Email is required.")
        }

        if (!email.contains("@")) {
            return AuthResult.Error("Enter a valid email address.")
        }

        if (password.length < 6) {
            return AuthResult.Error("Password must be at least 6 characters.")
        }

        return runCatching { operation() }.fold(
            onSuccess = { AuthResult.Success },
            onFailure = { error ->
                AuthResult.Error(
                    error.message ?: "Authentication failed. Please try again."
                )
            }
        )
    }
}
