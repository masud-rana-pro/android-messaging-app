package com.contactme.app.message

import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun observeMessages(conversationId: String): Flow<List<ChatMessage>>

    suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        text: String
    ): MessageResult
}
