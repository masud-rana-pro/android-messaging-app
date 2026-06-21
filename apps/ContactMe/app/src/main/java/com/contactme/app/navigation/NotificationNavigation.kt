package com.contactme.app.navigation

import android.content.Intent
import com.contactme.app.conversation.ConversationType

object NotificationNavigation {
    const val EXTRA_CONVERSATION_ID = "contactme.extra.CONVERSATION_ID"
    const val EXTRA_CHAT_TITLE = "contactme.extra.CHAT_TITLE"
    const val EXTRA_CHAT_PHOTO_URL = "contactme.extra.CHAT_PHOTO_URL"
    const val EXTRA_CONVERSATION_TYPE = "contactme.extra.CONVERSATION_TYPE"
    const val EXTRA_CALL_ID = "contactme.extra.CALL_ID"
    const val EXTRA_NOTIFICATION_TYPE = "contactme.extra.NOTIFICATION_TYPE"

    fun chatTargetFrom(intent: Intent?): ChatTarget? {
        val conversationId = intent?.getStringExtra(EXTRA_CONVERSATION_ID)
            ?.takeIf(String::isNotBlank)
            ?: return null
        val title = intent.getStringExtra(EXTRA_CHAT_TITLE)
            ?.takeIf(String::isNotBlank)
            ?: "ContactMe User"
        val photoUrl = intent.getStringExtra(EXTRA_CHAT_PHOTO_URL).orEmpty()
        val type = ConversationType.fromFirestore(
            intent.getStringExtra(EXTRA_CONVERSATION_TYPE)
        )

        return ChatTarget(
            title = title,
            conversationId = conversationId,
            photoUrl = photoUrl,
            type = type
        )
    }

    fun callIdFrom(intent: Intent?): String? {
        return intent?.getStringExtra(EXTRA_CALL_ID)?.takeIf(String::isNotBlank)
    }

    fun clearChatTarget(intent: Intent?) {
        intent?.removeExtra(EXTRA_CONVERSATION_ID)
        intent?.removeExtra(EXTRA_CHAT_TITLE)
        intent?.removeExtra(EXTRA_CHAT_PHOTO_URL)
        intent?.removeExtra(EXTRA_CONVERSATION_TYPE)
        intent?.removeExtra(EXTRA_CALL_ID)
        intent?.removeExtra(EXTRA_NOTIFICATION_TYPE)
    }
}
