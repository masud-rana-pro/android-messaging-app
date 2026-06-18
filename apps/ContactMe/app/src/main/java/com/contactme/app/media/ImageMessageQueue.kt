package com.contactme.app.media

import android.net.Uri
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ImageMessageQueue @Inject constructor(
    private val workManager: WorkManager,
    private val pendingMediaStore: PendingMediaStore
) {
    suspend fun enqueue(
        conversationId: String,
        senderId: String,
        imageUri: Uri
    ): ImageQueueResult {
        return runCatching {
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

    fun observe(workId: UUID): Flow<WorkInfo?> = workManager.getWorkInfoByIdFlow(workId)

    companion object {
        const val CONVERSATION_ID_KEY = "conversation_id"
        const val SENDER_ID_KEY = "sender_id"
        const val FILE_PATH_KEY = "file_path"
        const val ERROR_KEY = "error"
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
