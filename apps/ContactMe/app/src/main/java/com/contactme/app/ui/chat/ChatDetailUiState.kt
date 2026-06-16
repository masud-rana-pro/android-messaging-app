package com.contactme.app.ui.chat

import com.contactme.app.message.ChatMessage

data class ChatDetailUiState(
    val currentUserId: String = "",
    val messageText: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val isLoadingMessages: Boolean = false,
    val isSending: Boolean = false,
    val errorMessage: String? = null
)
