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
class ImageMessageWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val messageRepository: MessageRepository
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        val conversationId = inputData.getString(ImageMessageQueue.CONVERSATION_ID_KEY).orEmpty()
        val senderId = inputData.getString(ImageMessageQueue.SENDER_ID_KEY).orEmpty()
        val filePath = inputData.getString(ImageMessageQueue.FILE_PATH_KEY).orEmpty()
        
        Log.d(TAG, "doWork start. conversationId: $conversationId, filePath: $filePath")
        val pendingFile = File(filePath)

        if (conversationId.isBlank() || senderId.isBlank() || !pendingFile.exists()) {
            Log.e(TAG, "Validation failed: file exists=${pendingFile.exists()}")
            return Result.failure(
                workDataOf(ImageMessageQueue.ERROR_KEY to "This photo is no longer available.")
            )
        }

        Log.d(TAG, "Calling messageRepository.sendImageMessage...")
        return when (
            val result = messageRepository.sendImageMessage(
                conversationId = conversationId,
                senderId = senderId,
                imageUri = Uri.fromFile(pendingFile)
            )
        ) {
            MessageResult.Success -> {
                Log.d(TAG, "Image message sent successfully. Deleting temp file.")
                pendingFile.delete()
                Result.success()
            }

            is MessageResult.Error -> {
                Log.e(TAG, "sendImageMessage failed: ${result.message}")
                if (runAttemptCount < MAX_RETRY_COUNT - 1) {
                    Log.d(TAG, "Retrying work...")
                    Result.retry()
                } else {
                    Log.e(TAG, "Max retries reached. Failing work.")
                    Result.failure(
                        workDataOf(ImageMessageQueue.ERROR_KEY to (result.message.ifBlank { "Unknown upload error." }))
                    )
                }
            }
        }
    }

    private companion object {
        const val TAG = "ImageMessageWorker"
        const val MAX_RETRY_COUNT = 3
    }
}
