package com.contactme.app.profile

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class FirebaseProfilePhotoRepository @Inject constructor(
    private val firebaseStorage: FirebaseStorage
) : ProfilePhotoRepository {
    override suspend fun uploadProfilePhoto(
        userId: String,
        photoUri: Uri
    ): ProfilePhotoResult {
        if (userId.isBlank()) {
            return ProfilePhotoResult.Error("Session expired. Please log in again.")
        }

        return runCatching {
            val photoReference = firebaseStorage.reference
                .child("profile_photos")
                .child(userId)
                .child("profile.jpg")

            photoReference.putFile(photoUri).await()
            photoReference.downloadUrl.await().toString()
        }.fold(
            onSuccess = { photoUrl -> ProfilePhotoResult.Success(photoUrl) },
            onFailure = {
                ProfilePhotoResult.Error("We could not upload your profile photo. Please try again.")
            }
        )
    }
}
