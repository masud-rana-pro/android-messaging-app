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
                    userId = document.id,
                    displayName = document.getString("displayName").orEmpty(),
                    username = document.getString("username").orEmpty()
                )
            }
        }.getOrNull()
    }

    override suspend fun searchProfiles(
        usernameQuery: String,
        currentUserId: String
    ): List<UserProfile> {
        val normalizedQuery = usernameQuery.trim().lowercase()

        if (normalizedQuery.length < MIN_USERNAME_SEARCH_LENGTH) {
            return emptyList()
        }

        return runCatching {
            firestore.collection(USERS_COLLECTION)
                .orderBy("username")
                .startAt(normalizedQuery)
                .endAt("$normalizedQuery\uf8ff")
                .limit(10)
                .get()
                .await()
                .documents
                .filter { document -> document.id != currentUserId }
                .map { document ->
                    UserProfile(
                        userId = document.id,
                        displayName = document.getString("displayName").orEmpty(),
                        username = document.getString("username").orEmpty()
                    )
                }
        }.getOrDefault(emptyList())
    }

    override suspend fun saveProfile(
        userId: String,
        displayName: String,
        username: String
    ): ProfileResult {
        return runCatching {
            val profileDocument = firestore.collection(USERS_COLLECTION).document(userId)
            val normalizedUsername = username.trim().lowercase()
            val usernameDocument = firestore.collection(USERNAMES_COLLECTION).document(normalizedUsername)

            firestore.runTransaction { transaction ->
                val profileSnapshot = transaction.get(profileDocument)
                val usernameSnapshot = transaction.get(usernameDocument)
                val existingUsernameOwner = usernameSnapshot.getString("userId")

                if (usernameSnapshot.exists() && existingUsernameOwner != userId) {
                    throw UsernameTakenException()
                }

                val oldUsername = profileSnapshot.getString("username")
                if (!oldUsername.isNullOrBlank() && oldUsername != normalizedUsername) {
                    transaction.delete(
                        firestore.collection(USERNAMES_COLLECTION).document(oldUsername)
                    )
                }

                val profileData = mutableMapOf<String, Any>(
                    "displayName" to displayName.trim(),
                    "username" to normalizedUsername,
                    PROFILE_COMPLETE_FIELD to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                if (!profileSnapshot.exists()) {
                    profileData["createdAt"] = FieldValue.serverTimestamp()
                }

                val usernameData = mapOf(
                    "userId" to userId,
                    "displayName" to displayName.trim(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                transaction.set(profileDocument, profileData, SetOptions.merge())
                transaction.set(usernameDocument, usernameData, SetOptions.merge())
            }.await()
        }.fold(
            onSuccess = { ProfileResult.Success },
            onFailure = { error ->
                if (error.isUsernameTakenError()) {
                    ProfileResult.Error("This username is already taken.")
                } else {
                    ProfileResult.Error("We could not save your profile. Please try again.")
                }
            }
        )
    }

    private class UsernameTakenException : Exception()

    private fun Throwable.isUsernameTakenError(): Boolean {
        return this is UsernameTakenException || cause?.isUsernameTakenError() == true
    }

    private companion object {
        const val USERS_COLLECTION = "users"
        const val USERNAMES_COLLECTION = "usernames"
        const val PROFILE_COMPLETE_FIELD = "profileComplete"
        const val MIN_USERNAME_SEARCH_LENGTH = 3
    }
}
