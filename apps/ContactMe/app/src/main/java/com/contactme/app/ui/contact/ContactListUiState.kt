package com.contactme.app.ui.contact

import com.contactme.app.profile.UserProfile

data class ContactListUiState(
    val contacts: List<UserProfile> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null
)
