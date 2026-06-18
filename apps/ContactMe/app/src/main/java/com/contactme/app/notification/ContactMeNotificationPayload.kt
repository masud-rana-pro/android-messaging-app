package com.contactme.app.notification

import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage

internal data class ContactMeNotificationPayload(
    val notificationId: Int,
    val channelId: String,
    val conversationId: String,
    val title: String,
    val photoUrl: String,
    val body: String,
    val priority: Int,
    val category: String,
    val groupKey: String
) {
    companion object {
        fun from(message: RemoteMessage): ContactMeNotificationPayload? {
            return fromData(
                data = message.data,
                fallbackTitle = message.notification?.title,
                fallbackBody = message.notification?.body,
                messageId = message.messageId
            )
        }

        fun fromData(
            data: Map<String, String>,
            fallbackTitle: String? = null,
            fallbackBody: String? = null,
            messageId: String? = null
        ): ContactMeNotificationPayload? {
            val type = data["type"].orEmpty().ifBlank { MESSAGE_TYPE }
            val conversationId = data["conversationId"].orEmpty()

            if (type == MESSAGE_TYPE && conversationId.isBlank()) return null

            val channelId = when (type) {
                CALL_TYPE -> ContactMeNotificationChannels.CALLS_CHANNEL_ID
                SYSTEM_TYPE -> ContactMeNotificationChannels.SYSTEM_CHANNEL_ID
                else -> ContactMeNotificationChannels.MESSAGES_CHANNEL_ID
            }
            val isMessage = channelId == ContactMeNotificationChannels.MESSAGES_CHANNEL_ID
            val stableKey = conversationId.ifBlank { messageId.orEmpty() }.ifBlank { type }

            return ContactMeNotificationPayload(
                notificationId = stableKey.hashCode() and Int.MAX_VALUE,
                channelId = channelId,
                conversationId = conversationId,
                title = data["title"] ?: fallbackTitle ?: DEFAULT_TITLE,
                photoUrl = data["photoUrl"].orEmpty(),
                body = data["body"] ?: fallbackBody ?: DEFAULT_BODY,
                priority = if (channelId == ContactMeNotificationChannels.SYSTEM_CHANNEL_ID) {
                    NotificationCompat.PRIORITY_DEFAULT
                } else {
                    NotificationCompat.PRIORITY_HIGH
                },
                category = if (isMessage) {
                    NotificationCompat.CATEGORY_MESSAGE
                } else {
                    NotificationCompat.CATEGORY_STATUS
                },
                groupKey = if (conversationId.isNotBlank()) {
                    "conversation_$conversationId"
                } else {
                    "contactme_$type"
                }
            )
        }

        private const val MESSAGE_TYPE = "message"
        private const val CALL_TYPE = "call"
        private const val SYSTEM_TYPE = "system"
        private const val DEFAULT_TITLE = "ContactMe"
        private const val DEFAULT_BODY = "You have a new update."
    }
}
