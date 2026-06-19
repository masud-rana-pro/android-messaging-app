package com.contactme.app.media

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class DocumentMessageQueue @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager,
    private val pendingMediaStore: PendingMediaStore
) {
    suspend fun enqueue(
        conversationId: String,
        senderId: String,
        documentUri: Uri,
        preferredFileName: String? = null,
        preferredMimeType: String? = null
    ): DocumentQueueResult = runCatching {
        val mimeType = preferredMimeType ?: context.contentResolver.getType(documentUri).orEmpty()
        val fileName = preferredFileName ?: resolveFileName(documentUri)
        if (mimeType !in ALLOWED_TYPES) {
            throw MediaUploadException("Choose a PDF, text, DOC, or DOCX file.")
        }
        val pendingFile = pendingMediaStore.preserve(documentUri)
        if (pendingFile.length() !in 1..MAX_SIZE_BYTES) {
            pendingFile.delete()
            throw MediaUploadException("Choose a document smaller than 25 MB.")
        }

        val request = OneTimeWorkRequestBuilder<DocumentMessageWorker>()
            .setInputData(
                workDataOf(
                    CONVERSATION_ID_KEY to conversationId,
                    SENDER_ID_KEY to senderId,
                    FILE_PATH_KEY to pendingFile.absolutePath,
                    FILE_NAME_KEY to fileName,
                    MIME_TYPE_KEY to mimeType,
                    FILE_SIZE_KEY to pendingFile.length()
                )
            )
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()
        workManager.enqueueUniqueWork(
            "document-message-${request.id}",
            ExistingWorkPolicy.KEEP,
            request
        )
        DocumentQueueResult.Queued(
            QueuedDocumentMessage(
                request.id,
                Uri.fromFile(pendingFile).toString(),
                fileName,
                mimeType
            )
        )
    }.getOrElse { error ->
        DocumentQueueResult.Error(
            (error as? MediaUploadException)?.userMessage
                ?: "We could not prepare this document. Please try again."
        )
    }

    fun observe(workId: UUID): Flow<WorkInfo?> = workManager.getWorkInfoByIdFlow(workId)

    private fun resolveFileName(uri: Uri): String {
        return context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
            ?.take(255)
            ?.takeIf(String::isNotBlank)
            ?: "document"
    }

    companion object {
        const val CONVERSATION_ID_KEY = "conversation_id"
        const val SENDER_ID_KEY = "sender_id"
        const val FILE_PATH_KEY = "file_path"
        const val FILE_NAME_KEY = "file_name"
        const val MIME_TYPE_KEY = "mime_type"
        const val FILE_SIZE_KEY = "file_size"
        const val ERROR_KEY = "error"
        const val MAX_SIZE_BYTES = 25L * 1024 * 1024
        val ALLOWED_TYPES = setOf(
            "application/pdf",
            "text/plain",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        )
    }
}

data class QueuedDocumentMessage(
    val workId: UUID,
    val localUri: String,
    val fileName: String,
    val mimeType: String
)

sealed interface DocumentQueueResult {
    data class Queued(val message: QueuedDocumentMessage) : DocumentQueueResult
    data class Error(val message: String) : DocumentQueueResult
}
