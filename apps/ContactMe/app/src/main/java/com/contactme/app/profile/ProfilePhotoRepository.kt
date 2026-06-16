package com.contactme.app.profile

import android.net.Uri

interface ProfilePhotoRepository {
    suspend fun uploadProfilePhoto(
        userId: String,
        photoUri: Uri
    ): ProfilePhotoResult
}
