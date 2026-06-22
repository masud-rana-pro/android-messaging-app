package com.contactme.app.media

import android.util.Log
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
        
        Log.d(TAG, "doWork start. conversationId: $conversationId, filePath: $filePath")
        val pendingFile = File(filePath)
        if (conversationId.isBlank() || senderId.isBlank() || !pendingFile.exists()) {
            Log.e(TAG, "Validation failed: file exists=${pendingFile.exists()}")
            return Result.failure(workDataOf(VoiceMessageQueue.ERROR_KEY to "This voice message is no longer available."))
        }

        Log.d(TAG, "Calling messageRepository.sendVoiceMessage...")
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
                Log.d(TAG, "Voice message sent successfully. Deleting temp file.")
                pendingFile.delete()
                Result.success()
            }
            is MessageResult.Error -> {
                Log.e(TAG, "sendVoiceMessage failed: ${result.message}")
                if (runAttemptCount < 2) {
                    Log.d(TAG, "Retrying work...")
                    Result.retry()
                } else {
                    Log.e(TAG, "Max retries reached. Failing work.")
                    Result.failure(
                        workDataOf(VoiceMessageQueue.ERROR_KEY to (result.message.ifBlank { "Unknown upload error." }))
                    )
                }
            }
        }
    }

    private companion object {
        const val TAG = "VoiceMessageWorker"
    }
}
