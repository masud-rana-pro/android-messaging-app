package com.contactme.app.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.contactme.app.MainActivity
import com.contactme.app.R
import com.contactme.app.navigation.NotificationNavigation
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.abs

class ContactMeNotificationRenderer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun show(message: RemoteMessage) {
        if (!canShowNotifications()) return

        val payload = ContactMeNotificationPayload.from(message)
        val pendingIntent = PendingIntent.getActivity(
            context,
            payload.notificationId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(NotificationNavigation.EXTRA_CONVERSATION_ID, payload.conversationId)
                putExtra(NotificationNavigation.EXTRA_CHAT_TITLE, payload.title)
                putExtra(NotificationNavigation.EXTRA_CHAT_PHOTO_URL, payload.photoUrl)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, payload.channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(payload.title)
            .setContentText(payload.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(payload.body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(payload.priority)
            .build()

        NotificationManagerCompat.from(context).notify(
            payload.notificationId,
            notification
        )
    }

    private fun canShowNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}

private data class ContactMeNotificationPayload(
    val notificationId: Int,
    val channelId: String,
    val conversationId: String,
    val title: String,
    val photoUrl: String,
    val body: String,
    val priority: Int
) {
    companion object {
        fun from(message: RemoteMessage): ContactMeNotificationPayload {
            val type = message.data["type"].orEmpty()
            val conversationId = message.data["conversationId"].orEmpty()
            val photoUrl = message.data["photoUrl"].orEmpty()
            val title = message.data["title"]
                ?: message.notification?.title
                ?: DEFAULT_TITLE
            val body = message.data["body"]
                ?: message.notification?.body
                ?: DEFAULT_BODY
            val channelId = when (type) {
                "call" -> ContactMeNotificationChannels.CALLS_CHANNEL_ID
                "system" -> ContactMeNotificationChannels.SYSTEM_CHANNEL_ID
                else -> ContactMeNotificationChannels.MESSAGES_CHANNEL_ID
            }
            val priority = when (channelId) {
                ContactMeNotificationChannels.SYSTEM_CHANNEL_ID -> NotificationCompat.PRIORITY_DEFAULT
                else -> NotificationCompat.PRIORITY_HIGH
            }

            return ContactMeNotificationPayload(
                notificationId = stableNotificationId(conversationId.ifBlank { message.messageId.orEmpty() }),
                channelId = channelId,
                conversationId = conversationId,
                title = title,
                photoUrl = photoUrl,
                body = body,
                priority = priority
            )
        }

        private fun stableNotificationId(key: String): Int {
            return abs(key.ifBlank { DEFAULT_TITLE }.hashCode())
        }

        private const val DEFAULT_TITLE = "ContactMe"
        private const val DEFAULT_BODY = "You have a new update."
    }
}
