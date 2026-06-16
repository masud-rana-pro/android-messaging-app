package com.contactme.app.conversation

data class ConversationPreview(
    val conversationId: String,
    val otherUserId: String,
    val title: String,
    val subtitle: String,
    val updatedAtMillis: Long,
    val hasUnreadMessages: Boolean
)
