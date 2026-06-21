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

class ImageMessageQueue @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager,
    private val pendingMediaStore: PendingMediaStore
) {
    suspend fun enqueue(
        conversationId: String,
        senderId: String,
        imageUri: Uri
    ): ImageQueueResult {
        return runCatching {
            val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
            if (!mimeType.startsWith("image/")) {
                throw MediaUploadException("Only image files can be uploaded here.")
            }

            val fileSize = resolveFileSize(imageUri)
            if (fileSize > MAX_IMAGE_SIZE_BYTES) {
                throw MediaUploadException("Choose a photo smaller than 10 MB.")
            }

            val pendingFile = pendingMediaStore.preserve(imageUri)
            val request = OneTimeWorkRequestBuilder<ImageMessageWorker>()
                .setInputData(
                    workDataOf(
                        CONVERSATION_ID_KEY to conversationId,
                        SENDER_ID_KEY to senderId,
                        FILE_PATH_KEY to pendingFile.absolutePath
                    )
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .build()

            workManager.enqueueUniqueWork(
                "image-message-${request.id}",
                ExistingWorkPolicy.KEEP,
                request
            )
            ImageQueueResult.Queued(
                QueuedImageMessage(
                    workId = request.id,
                    localUri = Uri.fromFile(pendingFile).toString()
                )
            )
        }.getOrElse { error ->
            ImageQueueResult.Error(
                (error as? MediaUploadException)?.userMessage
                    ?: "We could not prepare this photo. Please try again."
            )
        }
    }

    private fun resolveFileSize(uri: Uri): Long {
        return context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getLong(0) else 0L
            } ?: 0L
    }

    fun observe(workId: UUID): Flow<WorkInfo?> = workManager.getWorkInfoByIdFlow(workId)

    companion object {
        const val CONVERSATION_ID_KEY = "conversation_id"
        const val SENDER_ID_KEY = "sender_id"
        const val FILE_PATH_KEY = "file_path"
        const val ERROR_KEY = "error"
        const val MAX_IMAGE_SIZE_BYTES = 10L * 1024 * 1024
    }
}

data class QueuedImageMessage(
    val workId: UUID,
    val localUri: String
)

sealed interface ImageQueueResult {
    data class Queued(val message: QueuedImageMessage) : ImageQueueResult
    data class Error(val message: String) : ImageQueueResult
}
