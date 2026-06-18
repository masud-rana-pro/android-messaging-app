package com.contactme.app.ui.group

import com.contactme.app.profile.UserProfile

data class GroupCreationUiState(
    val title: String = "",
    val contacts: List<UserProfile> = emptyList(),
    val selectedUserIds: Set<String> = emptySet(),
    val isLoadingContacts: Boolean = true,
    val isCreating: Boolean = false,
    val errorMessage: String? = null
)
