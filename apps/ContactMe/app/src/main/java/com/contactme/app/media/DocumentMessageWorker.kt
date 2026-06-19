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
class DocumentMessageWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val messageRepository: MessageRepository
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        val conversationId = inputData.getString(DocumentMessageQueue.CONVERSATION_ID_KEY).orEmpty()
        val senderId = inputData.getString(DocumentMessageQueue.SENDER_ID_KEY).orEmpty()
        val filePath = inputData.getString(DocumentMessageQueue.FILE_PATH_KEY).orEmpty()
        val fileName = inputData.getString(DocumentMessageQueue.FILE_NAME_KEY).orEmpty()
        val mimeType = inputData.getString(DocumentMessageQueue.MIME_TYPE_KEY).orEmpty()
        val fileSize = inputData.getLong(DocumentMessageQueue.FILE_SIZE_KEY, 0L)
        val pendingFile = File(filePath)
        if (conversationId.isBlank() || senderId.isBlank() || fileName.isBlank() || !pendingFile.exists()) {
            return Result.failure(workDataOf(DocumentMessageQueue.ERROR_KEY to "This document is no longer available."))
        }

        return when (
            val result = messageRepository.sendDocumentMessage(
                conversationId,
                senderId,
                Uri.fromFile(pendingFile),
                fileName,
                mimeType,
                fileSize
            )
        ) {
            MessageResult.Success -> {
                pendingFile.delete()
                Result.success()
            }
            is MessageResult.Error -> {
                if (runAttemptCount < 2) Result.retry()
                else Result.failure(workDataOf(DocumentMessageQueue.ERROR_KEY to result.message))
            }
        }
    }
}
