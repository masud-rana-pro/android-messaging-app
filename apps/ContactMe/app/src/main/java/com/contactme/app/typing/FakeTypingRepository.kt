package com.contactme.app.typing

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeTypingRepository @Inject constructor() : TypingRepository {
    private val typingByConversation = MutableStateFlow<Map<String, Map<String, Boolean>>>(emptyMap())

    override fun observeOtherTyping(
        conversationId: String,
        currentUserId: String
    ): Flow<Boolean> {
        return typingByConversation.map { conversations ->
            conversations[conversationId]
                .orEmpty()
                .any { (userId, isTyping) -> userId != currentUserId && isTyping }
        }
    }

    override suspend fun setTyping(
        conversationId: String,
        userId: String,
        isTyping: Boolean
    ) {
        typingByConversation.value = typingByConversation.value.toMutableMap().also { conversations ->
            val typingUsers = conversations[conversationId].orEmpty().toMutableMap()
            typingUsers[userId] = isTyping
            conversations[conversationId] = typingUsers
        }
    }
}
