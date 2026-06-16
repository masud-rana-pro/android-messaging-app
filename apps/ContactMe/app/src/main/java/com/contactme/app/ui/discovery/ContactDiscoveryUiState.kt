package com.contactme.app.ui.discovery

import com.contactme.app.profile.UserProfile

data class ContactDiscoveryUiState(
    val query: String = "",
    val results: List<UserProfile> = emptyList(),
    val isSearching: Boolean = false,
    val message: String? = null
)
