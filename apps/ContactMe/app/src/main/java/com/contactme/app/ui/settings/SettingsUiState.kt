package com.contactme.app.ui.settings

data class SettingsUiState(
    val displayName: String = "ContactMe User",
    val username: String = "contactme",
    val isLoadingProfile: Boolean = true,
    val errorMessage: String? = null
)
