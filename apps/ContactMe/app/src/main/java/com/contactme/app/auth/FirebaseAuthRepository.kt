package com.contactme.app.auth

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.resume

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

    override suspend fun requestPhoneOtp(
        phoneNumber: String,
        activity: Activity
    ): PhoneOtpResult {
        if (phoneNumber.isBlank()) {
            return PhoneOtpResult.Error("Phone number is required.")
        }

        if (phoneNumber.filter(Char::isDigit).length < 10) {
            return PhoneOtpResult.Error("Enter a valid phone number.")
        }

        return suspendCancellableCoroutine { continuation ->
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                    firebaseAuth.signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            if (!continuation.isActive) return@addOnCompleteListener

                            if (task.isSuccessful) {
                                continuation.resume(PhoneOtpResult.Verified)
                            } else {
                                continuation.resume(
                                    PhoneOtpResult.Error(
                                        task.exception?.message ?: "Phone verification failed."
                                    )
                                )
                            }
                        }
                }

                override fun onVerificationFailed(exception: FirebaseException) {
                    if (!continuation.isActive) return

                    continuation.resume(
                        PhoneOtpResult.Error(
                            exception.message ?: "Could not send OTP. Please try again."
                        )
                    )
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    if (!continuation.isActive) return

                    continuation.resume(PhoneOtpResult.CodeSent(verificationId))
                }
            }

            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber.trim())
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    override suspend fun verifyPhoneOtp(
        verificationId: String,
        otpCode: String
    ): AuthResult {
        if (verificationId.isBlank()) {
            return AuthResult.Error("OTP session expired. Send a new code.")
        }

        if (otpCode.length != 6) {
            return AuthResult.Error("Enter the 6-digit OTP code.")
        }

        val credential = PhoneAuthProvider.getCredential(
            verificationId,
            otpCode
        )

        return runCatching {
            firebaseAuth.signInWithCredential(credential).await()
        }.fold(
            onSuccess = { AuthResult.Success },
            onFailure = { error ->
                AuthResult.Error(
                    error.message ?: "OTP verification failed. Please try again."
                )
            }
        )
    }
}
