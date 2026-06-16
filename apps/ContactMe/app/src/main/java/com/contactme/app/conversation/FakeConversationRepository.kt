package com.contactme.app.conversation

import kotlinx.coroutines.delay
import javax.inject.Inject

class FakeConversationRepository @Inject constructor() : ConversationRepository {
    private val conversationIds = mutableSetOf<String>()

    override suspend fun getOrCreateDirectConversation(
        currentUserId: String,
        otherUserId: String
    ): ConversationResult {
        delay(250)

        if (currentUserId == otherUserId) {
            return ConversationResult.Error("You cannot open a chat with yourself.")
        }

        val conversationId = listOf(currentUserId, otherUserId)
            .sorted()
            .joinToString(separator = "__")

        conversationIds.add(conversationId)

        return ConversationResult.Success(conversationId)
    }
}
