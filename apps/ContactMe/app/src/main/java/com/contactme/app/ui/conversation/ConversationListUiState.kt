package com.contactme.app.ui.conversation

import com.contactme.app.conversation.ConversationPreview

data class ConversationListUiState(
    val conversations: List<ConversationPreview> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null
)
