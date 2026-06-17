package com.contactme.app.media

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class CloudinaryUploadClient @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    suspend fun upload(
        uri: Uri,
        fileName: String
    ): CloudinaryUpload {
        return withContext(Dispatchers.IO) {
            val mimeType = context.contentResolver.getType(uri) ?: DEFAULT_IMAGE_MIME_TYPE
            val fileBytes = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            } ?: throw IllegalStateException("File is unavailable.")
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", CLOUDINARY_UPLOAD_PRESET)
                .addFormDataPart(
                    "file",
                    fileName,
                    fileBytes.toRequestBody(mimeType.toMediaTypeOrNull())
                )
                .build()
            val request = Request.Builder()
                .url(CLOUDINARY_UPLOAD_URL)
                .post(requestBody)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw IllegalStateException("Cloudinary upload failed.")
                }

                val json = JSONObject(responseBody)
                CloudinaryUpload(
                    secureUrl = json.getString("secure_url"),
                    publicId = json.getString("public_id"),
                    mimeType = mimeType
                )
            }
        }
    }

    private companion object {
        const val DEFAULT_IMAGE_MIME_TYPE = "image/jpeg"
        const val CLOUDINARY_CLOUD_NAME = "dew95musb"
        const val CLOUDINARY_UPLOAD_PRESET = "contactme_unsigned"
        const val CLOUDINARY_UPLOAD_URL = "https://api.cloudinary.com/v1_1/$CLOUDINARY_CLOUD_NAME/auto/upload"
    }
}
