package com.contactme.app.media

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
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
        Log.d(TAG, "Enqueuing image message for conversation: $conversationId, URI: $imageUri")
        return runCatching {
            val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
            Log.d(TAG, "Resolved MIME type: $mimeType")
            if (!mimeType.startsWith("image/")) {
                throw MediaUploadException("Only image files can be uploaded here.")
            }

            val fileSize = resolveFileSize(imageUri)
            Log.d(TAG, "Resolved file size: $fileSize bytes")
            if (fileSize > MAX_IMAGE_SIZE_BYTES) {
                throw MediaUploadException("Choose a photo smaller than 10 MB.")
            }

            Log.d(TAG, "Preserving media in PendingMediaStore...")
            val pendingFile = pendingMediaStore.preserve(imageUri)
            Log.d(TAG, "Media preserved at: ${pendingFile.absolutePath}, exists: ${pendingFile.exists()}, size: ${pendingFile.length()}")
            
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

            Log.d(TAG, "Enqueuing unique work: image-message-${request.id}")
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
            Log.e(TAG, "Failed to enqueue image message", error)
            ImageQueueResult.Error(
                (error as? MediaUploadException)?.userMessage
                    ?: "We could not prepare this photo. Please try again."
            )
        }
    }

    private fun resolveFileSize(uri: Uri): Long {
        if (uri.scheme == "file") {
            return uri.path?.let { java.io.File(it).length() } ?: 0L
        }
        return context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getLong(0) else 0L
            } ?: 0L
    }

    fun observe(workId: UUID): Flow<WorkInfo?> = workManager.getWorkInfoByIdFlow(workId)

    companion object {
        const val TAG = "ImageMessageQueue"
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
