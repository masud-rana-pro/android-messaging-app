package com.contactme.app.ui.settings

import com.contactme.app.profile.PrivacySettings

data class SettingsUiState(
    val displayName: String = "ContactMe User",
    val username: String = "contactme",
    val photoUrl: String = "",
    val privacySettings: PrivacySettings = PrivacySettings(),
    val isLoadingProfile: Boolean = true,
    val isSavingPrivacy: Boolean = false,
    val errorMessage: String? = null
)
