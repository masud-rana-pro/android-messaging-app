package com.contactme.app.presence

import javax.inject.Inject

class FakePresenceRepository @Inject constructor() : PresenceRepository {
    private val onlineUsers = mutableSetOf<String>()

    override suspend fun markOnline(userId: String) {
        onlineUsers += userId
    }

    override suspend fun markOffline(userId: String) {
        onlineUsers -= userId
    }
}
