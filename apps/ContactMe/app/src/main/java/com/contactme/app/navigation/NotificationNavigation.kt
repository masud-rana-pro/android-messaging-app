package com.contactme.app.navigation

import android.content.Intent

object NotificationNavigation {
    const val EXTRA_CONVERSATION_ID = "contactme.extra.CONVERSATION_ID"
    const val EXTRA_CHAT_TITLE = "contactme.extra.CHAT_TITLE"
    const val EXTRA_CHAT_PHOTO_URL = "contactme.extra.CHAT_PHOTO_URL"

    fun chatTargetFrom(intent: Intent?): ChatTarget? {
        val conversationId = intent?.getStringExtra(EXTRA_CONVERSATION_ID)
            ?.takeIf(String::isNotBlank)
            ?: return null
        val title = intent.getStringExtra(EXTRA_CHAT_TITLE)
            ?.takeIf(String::isNotBlank)
            ?: "ContactMe User"
        val photoUrl = intent.getStringExtra(EXTRA_CHAT_PHOTO_URL).orEmpty()

        return ChatTarget(
            title = title,
            conversationId = conversationId,
            photoUrl = photoUrl
        )
    }
}
