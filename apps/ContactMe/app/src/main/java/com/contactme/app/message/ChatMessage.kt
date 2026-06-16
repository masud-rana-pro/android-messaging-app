package com.contactme.app.message

data class ChatMessage(
    val id: String,
    val senderId: String,
    val text: String,
    val sentAtMillis: Long,
    val status: MessageStatus = MessageStatus.Sent
)
