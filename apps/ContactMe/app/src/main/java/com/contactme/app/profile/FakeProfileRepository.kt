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

    override suspend fun searchProfiles(
        usernameQuery: String,
        currentUserId: String
    ): List<UserProfile> {
        delay(150)
        val normalizedQuery = usernameQuery.trim().lowercase()

        if (normalizedQuery.length < 3) {
            return emptyList()
        }

        return profiles.values
            .filter { profile ->
                profile.userId != currentUserId && profile.username.startsWith(normalizedQuery)
            }
            .take(10)
    }

    override suspend fun saveProfile(
        userId: String,
        displayName: String,
        username: String
    ): ProfileResult {
        delay(350)
        val normalizedUsername = username.trim().lowercase()
        val usernameOwner = profiles.values.firstOrNull { profile ->
            profile.username == normalizedUsername && profile.userId != userId
        }

        if (usernameOwner != null) {
            return ProfileResult.Error("This username is already taken.")
        }

        profiles[userId] = UserProfile(
            userId = userId,
            displayName = displayName.trim(),
            username = normalizedUsername
        )
        return ProfileResult.Success
    }
}
