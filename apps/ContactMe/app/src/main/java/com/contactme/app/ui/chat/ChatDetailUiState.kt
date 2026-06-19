package com.contactme.app.ui.chat

import com.contactme.app.message.ChatMessage
import com.contactme.app.conversation.ReadReceiptState
import com.contactme.app.presence.PresenceStatus
import com.contactme.app.message.MessageReply

data class ChatDetailUiState(
    val currentUserId: String = "",
    val messageText: String = "",
    val replyingTo: MessageReply? = null,
    val messages: List<ChatMessage> = emptyList(),
    val isLoadingMessages: Boolean = false,
    val isOtherUserTyping: Boolean = false,
    val peerPresence: PresenceStatus = PresenceStatus(),
    val readReceiptState: ReadReceiptState = ReadReceiptState(),
    val isSending: Boolean = false,
    val isSafetyActionInProgress: Boolean = false,
    val isChatBlocked: Boolean = false,
    val canUnblockChat: Boolean = false,
    val pendingImageUri: String = "",
    val failedImageUri: String = "",
    val pendingDocumentName: String = "",
    val failedDocumentUri: String = "",
    val failedDocumentName: String = "",
    val failedDocumentMimeType: String = "",
    val statusMessage: String? = null,
    val errorMessage: String? = null
)
