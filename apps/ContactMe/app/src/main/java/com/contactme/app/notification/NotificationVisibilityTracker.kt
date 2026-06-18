package com.contactme.app.notification

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

internal object NotificationVisibilityTracker {
    private val isAppForeground = AtomicBoolean(false)
    private val activeConversationId = AtomicReference<String?>(null)

    fun setAppForeground(isForeground: Boolean) {
        isAppForeground.set(isForeground)
    }

    fun setActiveConversation(conversationId: String?) {
        activeConversationId.set(conversationId?.takeIf { it.isNotBlank() })
    }

    fun clearActiveConversation(conversationId: String?) {
        activeConversationId.compareAndSet(conversationId, null)
    }

    fun shouldSuppress(conversationId: String): Boolean {
        return conversationId.isNotBlank() &&
            isAppForeground.get() &&
            activeConversationId.get() == conversationId
    }
}
