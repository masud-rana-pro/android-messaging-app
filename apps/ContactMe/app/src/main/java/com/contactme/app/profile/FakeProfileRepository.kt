package com.contactme.app.profile

import kotlinx.coroutines.delay
import javax.inject.Inject

class FakeProfileRepository @Inject constructor() : ProfileRepository {
    private val profiles = mutableMapOf<String, UserProfile>()

    override suspend fun isProfileComplete(userId: String): Boolean {
        delay(150)
        return profiles.containsKey(userId)
    }

    override suspend fun getProfile(userId: String): UserProfile? {
        delay(150)
        return profiles[userId]
    }

    override suspend fun saveProfile(
        userId: String,
        displayName: String,
        username: String
    ): ProfileResult {
        delay(350)
        profiles[userId] = UserProfile(
            displayName = displayName.trim(),
            username = username.trim().lowercase()
        )
        return ProfileResult.Success
    }
}
