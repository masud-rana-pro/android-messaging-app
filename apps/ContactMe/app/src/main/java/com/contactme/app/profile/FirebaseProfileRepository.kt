package com.contactme.app.profile

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ProfileRepository {
    override suspend fun isProfileComplete(userId: String): Boolean {
        return runCatching {
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
                .getBoolean(PROFILE_COMPLETE_FIELD) == true
        }.getOrDefault(false)
    }

    override suspend fun getProfile(userId: String): UserProfile? {
        return runCatching {
            val document = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()

            if (!document.exists()) {
                null
            } else {
                UserProfile(
                    displayName = document.getString("displayName").orEmpty(),
                    username = document.getString("username").orEmpty()
                )
            }
        }.getOrNull()
    }

    override suspend fun saveProfile(
        userId: String,
        displayName: String,
        username: String
    ): ProfileResult {
        return runCatching {
            val profileDocument = firestore.collection(USERS_COLLECTION).document(userId)
            val profileData = mutableMapOf<String, Any>(
                "displayName" to displayName.trim(),
                "username" to username.trim().lowercase(),
                PROFILE_COMPLETE_FIELD to true,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            if (!profileDocument.get().await().exists()) {
                profileData["createdAt"] = FieldValue.serverTimestamp()
            }

            profileDocument.set(profileData, SetOptions.merge()).await()
        }.fold(
            onSuccess = { ProfileResult.Success },
            onFailure = {
                ProfileResult.Error("We could not save your profile. Please try again.")
            }
        )
    }

    private companion object {
        const val USERS_COLLECTION = "users"
        const val PROFILE_COMPLETE_FIELD = "profileComplete"
    }
}
