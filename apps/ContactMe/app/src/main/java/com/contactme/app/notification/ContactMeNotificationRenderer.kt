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

class ContactMeNotificationRenderer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun show(message: RemoteMessage) {
        if (!canShowNotifications()) return

        val payload = ContactMeNotificationPayload.from(message) ?: return
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
            .setCategory(payload.category)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setGroup(payload.groupKey)
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
