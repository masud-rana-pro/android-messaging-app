package com.contactme.app.conversation

import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    fun observeConversationPreviews(currentUserId: String): Flow<List<ConversationPreview>>

    suspend fun getOrCreateDirectConversation(
        currentUserId: String,
        otherUserId: String
    ): ConversationResult

    suspend fun markConversationRead(
        conversationId: String,
        userId: String
    )
}
