package com.contactme.app.presence

import kotlinx.coroutines.flow.Flow

interface PresenceRepository {
    fun observeConversationPeerPresence(
        conversationId: String,
        currentUserId: String
    ): Flow<PresenceStatus>

    suspend fun markOnline(userId: String)

    suspend fun markOffline(userId: String)
}
