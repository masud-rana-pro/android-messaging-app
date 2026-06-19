package com.contactme.app.message

data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderDisplayName: String = "",
    val type: MessageType = MessageType.Text,
    val text: String,
    val mediaUrl: String = "",
    val mediaProvider: String = "",
    val mediaPublicId: String = "",
    val mimeType: String = "",
    val fileName: String = "",
    val fileSizeBytes: Long = 0L,
    val replyTo: MessageReply? = null,
    val isDeleted: Boolean = false,
    val sentAtMillis: Long,
    val status: MessageStatus = MessageStatus.Sent
)
