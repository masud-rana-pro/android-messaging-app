package com.contactme.app.ui.profile

data class ProfileSetupUiState(
    val displayName: String = "",
    val username: String = "",
    val photoUrl: String = "",
    val selectedPhotoUri: String = "",
    val isLoading: Boolean = false,
    val isExistingProfile: Boolean = false,
    val errorMessage: String? = null
)
