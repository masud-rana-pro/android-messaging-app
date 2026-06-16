package com.contactme.app.message

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeMessageRepository @Inject constructor() : MessageRepository {
    private val messagesByConversation = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())

    override fun observeMessages(conversationId: String): Flow<List<ChatMessage>> {
        return messagesByConversation.map { conversations ->
            conversations[conversationId].orEmpty()
        }
    }

    override suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        text: String
    ): MessageResult {
        val trimmedText = text.trim()

        if (trimmedText.isBlank()) {
            return MessageResult.Error("Type a message first.")
        }

        val message = ChatMessage(
            id = "fake-message-${System.currentTimeMillis()}",
            senderId = senderId,
            text = trimmedText,
            sentAtMillis = System.currentTimeMillis(),
            status = MessageStatus.Sent
        )

        messagesByConversation.value = messagesByConversation.value.toMutableMap().also {
            it[conversationId] = it[conversationId].orEmpty() + message
        }

        return MessageResult.Success
    }
}
