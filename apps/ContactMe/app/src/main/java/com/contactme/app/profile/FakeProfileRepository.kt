package com.contactme.app.profile

import kotlinx.coroutines.delay
import javax.inject.Inject

class FakeProfileRepository @Inject constructor() : ProfileRepository {
    private val completedProfiles = mutableSetOf<String>()

    override suspend fun isProfileComplete(userId: String): Boolean {
        delay(150)
        return completedProfiles.contains(userId)
    }

    override suspend fun saveProfile(
        userId: String,
        displayName: String,
        username: String
    ): ProfileResult {
        delay(350)
        completedProfiles.add(userId)
        return ProfileResult.Success
    }
}
