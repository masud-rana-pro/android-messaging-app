package com.contactme.app.profile

data class PrivacySettings(
    val lastSeenVisibility: PrivacyVisibility = PrivacyVisibility.Everyone,
    val profilePhotoVisibility: PrivacyVisibility = PrivacyVisibility.Everyone,
    val readReceiptsEnabled: Boolean = true
)
