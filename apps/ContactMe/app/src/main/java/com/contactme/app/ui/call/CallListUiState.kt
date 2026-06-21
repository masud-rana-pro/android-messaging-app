package com.contactme.app.ui.call

import com.contactme.app.call.CallSession

data class CallListUiState(
    val currentUserId: String = "",
    val calls: List<CallSession> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null
)
