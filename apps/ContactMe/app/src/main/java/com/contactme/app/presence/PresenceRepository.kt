package com.contactme.app.presence

interface PresenceRepository {
    suspend fun markOnline(userId: String)

    suspend fun markOffline(userId: String)
}
