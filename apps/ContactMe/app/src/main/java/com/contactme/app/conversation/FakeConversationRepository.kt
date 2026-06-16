package com.contactme.app.conversation

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FakeConversationRepository @Inject constructor() : ConversationRepository {
    private val conversations = MutableStateFlow<Map<String, ConversationPreview>>(emptyMap())

    override fun observeConversationPreviews(currentUserId: String): Flow<List<ConversationPreview>> {
        return conversations.map { previews ->
            previews.values
                .filter { preview -> preview.conversationId.contains(currentUserId) }
                .sortedByDescending { preview -> preview.updatedAtMillis }
        }
    }

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

        conversations.value = conversations.value.toMutableMap().also {
            it[conversationId] = ConversationPreview(
                conversationId = conversationId,
                otherUserId = otherUserId,
                title = "ContactMe User",
                subtitle = "No messages yet.",
                updatedAtMillis = System.currentTimeMillis(),
                hasUnreadMessages = false
            )
        }

        return ConversationResult.Success(conversationId)
    }

    override suspend fun markConversationRead(
        conversationId: String,
        userId: String
    ) {
        val preview = conversations.value[conversationId] ?: return

        conversations.value = conversations.value.toMutableMap().also {
            it[conversationId] = preview.copy(hasUnreadMessages = false)
        }
    }
}
