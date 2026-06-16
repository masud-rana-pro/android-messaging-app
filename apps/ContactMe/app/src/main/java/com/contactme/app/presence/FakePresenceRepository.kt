package com.contactme.app.presence

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePresenceRepository @Inject constructor() : PresenceRepository {
    private val onlineUsers = mutableSetOf<String>()
    private val emptyPresence = MutableStateFlow(PresenceStatus())

    override fun observeConversationPeerPresence(
        conversationId: String,
        currentUserId: String
    ): Flow<PresenceStatus> = emptyPresence

    override suspend fun markOnline(userId: String) {
        onlineUsers += userId
    }

    override suspend fun markOffline(userId: String) {
        onlineUsers -= userId
    }
}
