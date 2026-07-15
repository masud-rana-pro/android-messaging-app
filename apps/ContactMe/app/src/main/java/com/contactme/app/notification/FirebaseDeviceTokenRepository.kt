package com.contactme.app.notification

import android.content.Context
import android.provider.Settings
import com.contactme.app.auth.AuthRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class FirebaseDeviceTokenRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore,
    private val firebaseMessaging: FirebaseMessaging
) : DeviceTokenRepository {
    override suspend fun syncCurrentDeviceToken(userId: String): DeviceTokenResult {
        if (userId.isBlank()) {
            return DeviceTokenResult.Error("Session expired. Please log in again.")
        }

        return runCatching {
            val token = firebaseMessaging.token.await()
            saveDeviceToken(
                userId = userId,
                token = token
            )
        }.fold(
            onSuccess = { DeviceTokenResult.Success },
            onFailure = {
                DeviceTokenResult.Error("We could not prepare notifications right now.")
            }
        )
    }

    override suspend fun syncRefreshedToken(token: String): DeviceTokenResult {
        val userId = authRepository.currentUserId()
            ?: return DeviceTokenResult.Error("Session expired. Please log in again.")

        return runCatching {
            saveDeviceToken(
                userId = userId,
                token = token
            )
        }.fold(
            onSuccess = { DeviceTokenResult.Success },
            onFailure = {
                DeviceTokenResult.Error("We could not refresh notifications right now.")
            }
        )
    }

    override suspend fun removeCurrentDeviceToken(userId: String): DeviceTokenResult {
        if (userId.isBlank()) {
            return DeviceTokenResult.Error("We could not update notifications for this device.")
        }

        return runCatching {
            firestore.collection(USER_DEVICES_COLLECTION)
                .document(userId)
                .collection(DEVICES_COLLECTION)
                .document(deviceId())
                .delete()
                .await()
            firebaseMessaging.deleteToken().await()
        }.fold(
            onSuccess = { DeviceTokenResult.Success },
            onFailure = {
                DeviceTokenResult.Error("We could not update notifications for this device.")
            }
        )
    }

    private suspend fun saveDeviceToken(
        userId: String,
        token: String
    ) {
        // Mirror latest token to user profile for server-side lookup (Step 92)
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .set(mapOf("fcmToken" to token), SetOptions.merge())
            .await()

        firestore.collection(USER_DEVICES_COLLECTION)
            .document(userId)
            .collection(DEVICES_COLLECTION)
            .document(deviceId())
            .set(
                mapOf(
                    "token" to token,
                    "platform" to ANDROID_PLATFORM,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            .await()
    }

    private fun deviceId(): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: DEFAULT_DEVICE_ID
    }

    private companion object {
        const val USERS_COLLECTION = "users"
        const val USER_DEVICES_COLLECTION = "user_devices"
        const val DEVICES_COLLECTION = "devices"
        const val ANDROID_PLATFORM = "android"
        const val DEFAULT_DEVICE_ID = "android_device"
    }
}
