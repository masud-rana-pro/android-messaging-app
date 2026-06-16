package com.contactme.app.conversation

interface ConversationRepository {
    suspend fun getOrCreateDirectConversation(
        currentUserId: String,
        otherUserId: String
    ): ConversationResult
}
