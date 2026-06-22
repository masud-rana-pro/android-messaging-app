package com.contactme.app.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.math.max

class CloudinaryUploadClient @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    suspend fun upload(
        uri: Uri,
        fileName: String
    ): CloudinaryUpload {
        return withContext(Dispatchers.IO) {
            Log.d(TAG, "Starting image upload for: $uri")
            val mimeType = context.contentResolver.getType(uri) ?: DEFAULT_IMAGE_MIME_TYPE
            
            val fileBytes = runCatching {
                if (uri.scheme == "file") {
                    uri.path?.let { java.io.File(it).readBytes() }
                } else {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.readBytes()
                    }
                }
            }.getOrNull() ?: throw MediaUploadException("This photo is unavailable. Please choose another one.")

            Log.d(TAG, "Image size: ${fileBytes.size}, MIME: $mimeType")
            validateImageUpload(
                mimeType = mimeType,
                fileSizeBytes = fileBytes.size
            )

            val preparedImage = prepareImageForUpload(
                originalBytes = fileBytes,
                originalMimeType = mimeType
            )

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", CLOUDINARY_UPLOAD_PRESET)
                .addFormDataPart(
                    "file",
                    fileName,
                    preparedImage.bytes.toRequestBody(preparedImage.mimeType.toMediaTypeOrNull())
                )
                .build()
            val request = Request.Builder()
                .url(CLOUDINARY_UPLOAD_URL_IMAGE)
                .post(requestBody)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                Log.d(TAG, "Cloudinary image upload response code: ${response.code}")
                if (!response.isSuccessful) {
                    Log.e(TAG, "Cloudinary image upload failed: $responseBody")
                    throw MediaUploadException("We could not upload this photo. Please try again.")
                }

                val json = JSONObject(responseBody)
                Log.d(TAG, "Cloudinary image upload successful")
                CloudinaryUpload(
                    secureUrl = json.getString("secure_url"),
                    publicId = json.getString("public_id"),
                    mimeType = preparedImage.mimeType
                )
            }
        }
    }

    suspend fun uploadDocument(
        uri: Uri,
        fileName: String,
        mimeType: String
    ): CloudinaryUpload = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting document/raw upload for: $uri, MIME: $mimeType")
        
        val fileBytes = runCatching {
            if (uri.scheme == "file") {
                uri.path?.let { java.io.File(it).readBytes() }
            } else {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }
        }.getOrNull() ?: throw MediaUploadException("This file is unavailable. Please choose another one.")
        
        Log.d(TAG, "File size: ${fileBytes.size}")
        if (fileBytes.isEmpty()) {
            throw MediaUploadException("This file is empty. Please try again.")
        }
        if (fileBytes.size > MAX_DOCUMENT_SIZE_BYTES) {
            throw MediaUploadException("Choose a file smaller than 25 MB.")
        }
        if (mimeType !in ALLOWED_TYPES) {
            Log.e(TAG, "Unsupported MIME type: $mimeType")
            throw MediaUploadException("This file type is not supported.")
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("upload_preset", CLOUDINARY_UPLOAD_PRESET)
            .addFormDataPart(
                "file",
                fileName,
                fileBytes.toRequestBody(mimeType.toMediaTypeOrNull())
            )
            .build()
        val request = Request.Builder().url(CLOUDINARY_UPLOAD_URL_RAW).post(requestBody).build()

        okHttpClient.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            Log.d(TAG, "Cloudinary raw upload response code: ${response.code}")
            if (!response.isSuccessful) {
                Log.e(TAG, "Cloudinary raw upload failed: $responseBody")
                throw MediaUploadException("We could not upload this file. Please try again.")
            }
            val json = JSONObject(responseBody)
            Log.d(TAG, "Cloudinary raw upload successful")
            CloudinaryUpload(
                secureUrl = json.getString("secure_url"),
                publicId = json.getString("public_id"),
                mimeType = mimeType
            )
        }
    }

    private fun validateImageUpload(
        mimeType: String,
        fileSizeBytes: Int
    ) {
        if (!mimeType.startsWith(IMAGE_MIME_PREFIX)) {
            throw MediaUploadException("Only image files can be uploaded here.")
        }

        if (fileSizeBytes == 0) {
            throw MediaUploadException("This photo is empty. Please try again.")
        }

        if (fileSizeBytes > MAX_IMAGE_SIZE_BYTES) {
            throw MediaUploadException("Choose a photo smaller than 10 MB.")
        }
    }

    private fun prepareImageForUpload(
        originalBytes: ByteArray,
        originalMimeType: String
    ): PreparedImage {
        val options = BitmapFactory.Options()
        val bitmap = BitmapFactory.decodeByteArray(originalBytes, 0, originalBytes.size, options)
            ?: return PreparedImage(
                bytes = originalBytes,
                mimeType = originalMimeType
            )

        val resizedBitmap = bitmap.resizeToMaxDimension(MAX_UPLOAD_DIMENSION)
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(
            Bitmap.CompressFormat.JPEG,
            JPEG_UPLOAD_QUALITY,
            outputStream
        )
        val compressedBytes = outputStream.toByteArray()

        if (resizedBitmap !== bitmap) {
            resizedBitmap.recycle()
        }
        bitmap.recycle()

        return if (compressedBytes.isNotEmpty() && compressedBytes.size < originalBytes.size) {
            PreparedImage(
                bytes = compressedBytes,
                mimeType = DEFAULT_IMAGE_MIME_TYPE
            )
        } else {
            PreparedImage(
                bytes = originalBytes,
                mimeType = originalMimeType
            )
        }
    }

    private fun Bitmap.resizeToMaxDimension(maxDimension: Int): Bitmap {
        val largestDimension = max(width, height)

        if (largestDimension <= maxDimension) return this

        val scale = maxDimension.toFloat() / largestDimension.toFloat()
        val targetWidth = (width * scale).toInt().coerceAtLeast(1)
        val targetHeight = (height * scale).toInt().coerceAtLeast(1)

        return Bitmap.createScaledBitmap(this, targetWidth, targetHeight, true)
    }

    private data class PreparedImage(
        val bytes: ByteArray,
        val mimeType: String
    )

    private companion object {
        const val TAG = "CloudinaryUploadClient"
        const val DEFAULT_IMAGE_MIME_TYPE = "image/jpeg"
        const val IMAGE_MIME_PREFIX = "image/"
        const val MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024
        const val MAX_DOCUMENT_SIZE_BYTES = 25 * 1024 * 1024
        const val MAX_UPLOAD_DIMENSION = 1600
        const val JPEG_UPLOAD_QUALITY = 82
        const val CLOUDINARY_CLOUD_NAME = "dew95musb"
        const val CLOUDINARY_UPLOAD_PRESET = "contactme_unsigned"
        const val CLOUDINARY_BASE_URL = "https://api.cloudinary.com/v1_1/$CLOUDINARY_CLOUD_NAME"
        const val CLOUDINARY_UPLOAD_URL_IMAGE = "$CLOUDINARY_BASE_URL/image/upload"
        const val CLOUDINARY_UPLOAD_URL_RAW = "$CLOUDINARY_BASE_URL/raw/upload"
        val ALLOWED_TYPES = setOf(
            "application/pdf",
            "text/plain",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "audio/mp4",
            "audio/aac",
            "audio/mpeg",
            "audio/3gpp",
            "audio/ogg",
            "audio/wav",
            "audio/webm"
        )
    }
}
