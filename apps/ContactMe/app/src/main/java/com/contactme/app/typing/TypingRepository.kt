package com.contactme.app.typing

import kotlinx.coroutines.flow.Flow

interface TypingRepository {
    fun observeOtherTyping(
        conversationId: String,
        currentUserId: String
    ): Flow<Boolean>

    suspend fun setTyping(
        conversationId: String,
        userId: String,
        isTyping: Boolean
    )
}
