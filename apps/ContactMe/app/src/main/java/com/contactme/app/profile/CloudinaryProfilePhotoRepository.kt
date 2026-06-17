package com.contactme.app.profile

import android.net.Uri
import com.contactme.app.media.CloudinaryUploadClient
import com.contactme.app.media.MediaUploadException
import javax.inject.Inject

class CloudinaryProfilePhotoRepository @Inject constructor(
    private val cloudinaryUploadClient: CloudinaryUploadClient
) : ProfilePhotoRepository {
    override suspend fun uploadProfilePhoto(
        userId: String,
        photoUri: Uri
    ): ProfilePhotoResult {
        if (userId.isBlank()) {
            return ProfilePhotoResult.Error("Session expired. Please log in again.")
        }

        return runCatching {
            cloudinaryUploadClient.upload(
                uri = photoUri,
                fileName = PROFILE_PHOTO_FILE_NAME
            ).secureUrl
        }.fold(
            onSuccess = { photoUrl -> ProfilePhotoResult.Success(photoUrl) },
            onFailure = { error ->
                ProfilePhotoResult.Error(
                    (error as? MediaUploadException)?.userMessage
                        ?: "We could not upload your profile photo. Please try again."
                )
            }
        )
    }

    private companion object {
        const val PROFILE_PHOTO_FILE_NAME = "profile.jpg"
    }
}
