package com.contactme.app.ui.chat

import com.contactme.app.message.ChatMessage
import com.contactme.app.presence.PresenceStatus

data class ChatDetailUiState(
    val currentUserId: String = "",
    val messageText: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val isLoadingMessages: Boolean = false,
    val isOtherUserTyping: Boolean = false,
    val peerPresence: PresenceStatus = PresenceStatus(),
    val isSending: Boolean = false,
    val errorMessage: String? = null
)
