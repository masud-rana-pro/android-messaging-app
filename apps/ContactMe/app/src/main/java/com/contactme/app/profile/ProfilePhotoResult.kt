package com.contactme.app.profile

sealed interface ProfilePhotoResult {
    data class Success(val photoUrl: String) : ProfilePhotoResult
    data class Error(val message: String) : ProfilePhotoResult
}
