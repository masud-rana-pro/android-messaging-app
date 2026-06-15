package com.contactme.app.profile

interface ProfileRepository {
    suspend fun isProfileComplete(userId: String): Boolean

    suspend fun saveProfile(
        userId: String,
        displayName: String,
        username: String
    ): ProfileResult
}
