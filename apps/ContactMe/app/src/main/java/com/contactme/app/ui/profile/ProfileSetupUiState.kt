package com.contactme.app.ui.profile

data class ProfileSetupUiState(
    val displayName: String = "",
    val username: String = "",
    val isLoading: Boolean = false,
    val isExistingProfile: Boolean = false,
    val errorMessage: String? = null
)
