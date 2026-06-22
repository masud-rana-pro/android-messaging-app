package com.contactme.app.media

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.contactme.app.message.MessageRepository
import com.contactme.app.message.MessageResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

@HiltWorker
class VoiceMessageWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val messageRepository: MessageRepository
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        val conversationId = inputData.getString(VoiceMessageQueue.CONVERSATION_ID_KEY).orEmpty()
        val senderId = inputData.getString(VoiceMessageQueue.SENDER_ID_KEY).orEmpty()
        val filePath = inputData.getString(VoiceMessageQueue.FILE_PATH_KEY).orEmpty()
        val duration = inputData.getLong(VoiceMessageQueue.DURATION_KEY, 0L)
        
        val pendingFile = File(filePath)
        if (conversationId.isBlank() || senderId.isBlank() || !pendingFile.exists()) {
            return Result.failure(workDataOf(VoiceMessageQueue.ERROR_KEY to "This voice message is no longer available."))
        }

        return when (
            val result = messageRepository.sendVoiceMessage(
                conversationId,
                senderId,
                Uri.fromFile(pendingFile),
                duration,
                pendingFile.length()
            )
        ) {
            MessageResult.Success -> {
                pendingFile.delete()
                Result.success()
            }
            is MessageResult.Error -> {
                if (runAttemptCount < 2) Result.retry()
                else Result.failure(workDataOf(VoiceMessageQueue.ERROR_KEY to result.message))
            }
        }
    }
}
