package com.contactme.app.profile

import kotlinx.coroutines.delay
import javax.inject.Inject

class FakeProfileRepository @Inject constructor() : ProfileRepository {
    private val profiles = mutableMapOf<String, UserProfile>()
    private val privacySettings = mutableMapOf<String, PrivacySettings>()

    override suspend fun isProfileComplete(userId: String): Boolean {
        delay(150)
        return profiles.containsKey(userId)
    }

    override suspend fun getProfile(userId: String): UserProfile? {
        delay(150)
        return profiles[userId]
    }

    override suspend fun getPrivacySettings(userId: String): PrivacySettings {
        delay(150)
        return privacySettings[userId] ?: PrivacySettings()
    }

    override suspend fun searchProfiles(
        query: String,
        currentUserId: String
    ): List<UserProfile> {
        delay(150)
        val normalizedQuery = query.trim().lowercase()
        val normalizedPhoneQuery = normalizeBangladeshNumber(query)

        if (normalizedPhoneQuery != null) {
            return profiles.values
                .filter { profile ->
                    profile.userId != currentUserId && profile.phoneNumber == normalizedPhoneQuery
                }
                .take(10)
        }

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
        username: String,
        photoUrl: String
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
            username = normalizedUsername,
            phoneNumber = "",
            photoUrl = photoUrl
        )
        return ProfileResult.Success
    }

    override suspend fun savePrivacySettings(
        userId: String,
        privacySettings: PrivacySettings
    ): ProfileResult {
        delay(150)
        this.privacySettings[userId] = privacySettings
        return ProfileResult.Success
    }

    private fun normalizeBangladeshNumber(input: String): String? {
        val trimmed = input.trim()
        val digits = trimmed.filter(Char::isDigit)

        return when {
            trimmed.startsWith("+880") && digits.length == 13 -> "+$digits"
            digits.startsWith("880") && digits.length == 13 -> "+$digits"
            digits.startsWith("0") && digits.length == 11 -> "+880${digits.drop(1)}"
            digits.length == 10 && digits.startsWith("1") -> "+880$digits"
            else -> null
        }
    }
}
