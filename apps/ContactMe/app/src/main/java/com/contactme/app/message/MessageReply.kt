package com.contactme.app.message

data class MessageReply(
    val messageId: String,
    val senderName: String,
    val preview: String,
    val type: MessageType
)
