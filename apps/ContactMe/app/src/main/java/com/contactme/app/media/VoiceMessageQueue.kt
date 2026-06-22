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

class VoiceMessageQueue @Inject constructor(
    private val workManager: WorkManager,
    private val pendingMediaStore: PendingMediaStore
) {
    suspend fun enqueue(
        conversationId: String,
        senderId: String,
        audioUri: Uri,
        durationMillis: Long
    ): VoiceQueueResult = runCatching {
        val pendingFile = pendingMediaStore.preserve(audioUri)
        
        val request = OneTimeWorkRequestBuilder<VoiceMessageWorker>()
            .setInputData(
                workDataOf(
                    CONVERSATION_ID_KEY to conversationId,
                    SENDER_ID_KEY to senderId,
                    FILE_PATH_KEY to pendingFile.absolutePath,
                    DURATION_KEY to durationMillis
                )
            )
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()
        workManager.enqueueUniqueWork(
            "voice-message-${request.id}",
            ExistingWorkPolicy.KEEP,
            request
        )
        VoiceQueueResult.Queued(
            QueuedVoiceMessage(
                request.id,
                Uri.fromFile(pendingFile).toString(),
                durationMillis
            )
        )
    }.getOrElse { error ->
        VoiceQueueResult.Error(
            (error as? MediaUploadException)?.userMessage
                ?: "We could not prepare this voice message. Please try again."
        )
    }

    fun observe(workId: UUID): Flow<WorkInfo?> = workManager.getWorkInfoByIdFlow(workId)

    companion object {
        const val CONVERSATION_ID_KEY = "conversation_id"
        const val SENDER_ID_KEY = "sender_id"
        const val FILE_PATH_KEY = "file_path"
        const val DURATION_KEY = "duration"
        const val ERROR_KEY = "error"
    }
}

data class QueuedVoiceMessage(
    val workId: UUID,
    val localUri: String,
    val durationMillis: Long
)

sealed interface VoiceQueueResult {
    data class Queued(val message: QueuedVoiceMessage) : VoiceQueueResult
    data class Error(val message: String) : VoiceQueueResult
}
