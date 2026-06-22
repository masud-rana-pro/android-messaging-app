package com.contactme.app.ui.call

import com.contactme.app.call.CallSession
import com.contactme.app.profile.UserProfile

data class CallListUiState(
    val currentUserId: String = "",
    val calls: List<CallSession> = emptyList(),
    val profiles: Map<String, UserProfile> = emptyMap(),
    val isLoading: Boolean = true,
    val message: String? = null
)
