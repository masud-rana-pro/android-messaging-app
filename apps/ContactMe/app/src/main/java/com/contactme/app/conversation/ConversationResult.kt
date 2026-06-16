package com.contactme.app.conversation

sealed interface ConversationResult {
    data class Success(val conversationId: String) : ConversationResult
    data class Error(val message: String) : ConversationResult
}
