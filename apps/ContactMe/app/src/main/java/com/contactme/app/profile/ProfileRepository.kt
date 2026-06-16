package com.contactme.app.profile

interface ProfileRepository {
    suspend fun isProfileComplete(userId: String): Boolean

    suspend fun getProfile(userId: String): UserProfile?

    suspend fun searchProfiles(
        query: String,
        currentUserId: String
    ): List<UserProfile>

    suspend fun saveProfile(
        userId: String,
        displayName: String,
        username: String,
        photoUrl: String
    ): ProfileResult
}
