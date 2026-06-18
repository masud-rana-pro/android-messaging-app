package com.contactme.app.conversation

import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    fun observeConversationPreviews(currentUserId: String): Flow<List<ConversationPreview>>

    fun observeReadReceiptState(
        conversationId: String,
        currentUserId: String
    ): Flow<ReadReceiptState>

    suspend fun getOrCreateDirectConversation(
        currentUserId: String,
        otherUserId: String
    ): ConversationResult

    suspend fun createGroupConversation(
        currentUserId: String,
        title: String,
        memberUserIds: List<String>
    ): ConversationResult

    suspend fun markConversationRead(
        conversationId: String,
        userId: String
    )
}
