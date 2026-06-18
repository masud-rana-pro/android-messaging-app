package com.contactme.app.media

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PendingMediaStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun preserve(sourceUri: Uri): File = withContext(Dispatchers.IO) {
        val pendingDirectory = File(context.filesDir, PENDING_DIRECTORY).apply { mkdirs() }
        val existingFile = sourceUri.path
            ?.let(::File)
            ?.takeIf { sourceUri.scheme == FILE_SCHEME && it.parentFile == pendingDirectory && it.exists() }
        if (existingFile != null) return@withContext existingFile

        val targetFile = File(pendingDirectory, "${UUID.randomUUID()}.media")
        val inputStream = if (sourceUri.scheme == FILE_SCHEME) {
            sourceUri.path?.let(::File)?.inputStream()
        } else {
            context.contentResolver.openInputStream(sourceUri)
        } ?: throw MediaUploadException("This photo is unavailable. Please choose another one.")

        runCatching {
            inputStream.use { input -> targetFile.outputStream().use(input::copyTo) }
        }.onFailure {
            targetFile.delete()
        }.getOrThrow()

        targetFile
    }

    private companion object {
        const val PENDING_DIRECTORY = "pending_media"
        const val FILE_SCHEME = "file"
    }
}
