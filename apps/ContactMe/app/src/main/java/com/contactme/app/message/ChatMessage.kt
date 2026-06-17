package com.contactme.app.message

data class ChatMessage(
    val id: String,
    val senderId: String,
    val type: MessageType = MessageType.Text,
    val text: String,
    val mediaUrl: String = "",
    val sentAtMillis: Long,
    val status: MessageStatus = MessageStatus.Sent
)
