package com.contactme.app.navigation

import com.contactme.app.conversation.ConversationType

data class ChatTarget(
    val title: String,
    val conversationId: String?,
    val photoUrl: String = "",
    val type: ConversationType = ConversationType.Direct
)
