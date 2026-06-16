package com.contactme.app.profile

import android.net.Uri
import javax.inject.Inject

class FakeProfilePhotoRepository @Inject constructor() : ProfilePhotoRepository {
    override suspend fun uploadProfilePhoto(
        userId: String,
        photoUri: Uri
    ): ProfilePhotoResult {
        return ProfilePhotoResult.Success(photoUri.toString())
    }
}
