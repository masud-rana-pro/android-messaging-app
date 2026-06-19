package com.contactme.app.message

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun observeMessages(conversationId: String): Flow<List<ChatMessage>>

    suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        text: String
    ): MessageResult

    suspend fun sendImageMessage(
        conversationId: String,
        senderId: String,
        imageUri: Uri
    ): MessageResult

    suspend fun sendDocumentMessage(
        conversationId: String,
        senderId: String,
        documentUri: Uri,
        fileName: String,
        mimeType: String,
        fileSizeBytes: Long
    ): MessageResult
}
