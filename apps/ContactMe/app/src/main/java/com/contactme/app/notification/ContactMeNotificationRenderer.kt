package com.contactme.app.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.media.RingtoneManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.contactme.app.MainActivity
import com.contactme.app.R
import com.contactme.app.navigation.NotificationNavigation
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import okhttp3.OkHttpClient
import okhttp3.Request

class ContactMeNotificationRenderer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    suspend fun show(message: RemoteMessage) {
        if (!canShowNotifications()) return

        val payload = ContactMeNotificationPayload.from(message) ?: return
        if (NotificationVisibilityTracker.shouldSuppress(payload.conversationId)) return
        val pendingIntent = PendingIntent.getActivity(
            context,
            payload.notificationId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(NotificationNavigation.EXTRA_CONVERSATION_ID, payload.conversationId)
                putExtra(
                    NotificationNavigation.EXTRA_CONVERSATION_TYPE,
                    payload.conversationType.firestoreValue
                )
                putExtra(NotificationNavigation.EXTRA_CHAT_TITLE, payload.title)
                putExtra(NotificationNavigation.EXTRA_CHAT_PHOTO_URL, payload.photoUrl)
                putExtra(NotificationNavigation.EXTRA_CALL_ID, payload.callId)
                putExtra(NotificationNavigation.EXTRA_NOTIFICATION_TYPE, payload.type)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = loadLargeIcon(payload.photoUrl)
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
            .apply { largeIcon?.let(::setLargeIcon) }
            .setSound(
                when (payload.type) {
                    "incoming_call" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                    "message" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    else -> null
                }
            )
            .setVibrate(
                when (payload.type) {
                    "incoming_call" -> longArrayOf(0, 500, 350, 500, 350, 700)
                    "message" -> longArrayOf(0, 180, 120, 220)
                    else -> null
                }
            )
            .setOngoing(payload.type == "incoming_call")
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

    private fun loadLargeIcon(photoUrl: String): Bitmap? {
        if (!photoUrl.startsWith("https://")) return null
        return runCatching {
            okHttpClient.newCall(Request.Builder().url(photoUrl).get().build()).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val body = response.body ?: return@use null
                val contentLength = body.contentLength()
                if (contentLength > MAX_NOTIFICATION_IMAGE_BYTES) return@use null
                val bytes = body.bytes()
                if (bytes.size > MAX_NOTIFICATION_IMAGE_BYTES) return@use null
                decodeNotificationIcon(bytes)
            }
        }.getOrNull()
    }

    private fun decodeNotificationIcon(bytes: ByteArray): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sampleSize = 1
        while (bounds.outWidth / sampleSize > MAX_NOTIFICATION_ICON_PX * 2 ||
            bounds.outHeight / sampleSize > MAX_NOTIFICATION_ICON_PX * 2
        ) {
            sampleSize *= 2
        }
        return BitmapFactory.decodeByteArray(
            bytes,
            0,
            bytes.size,
            BitmapFactory.Options().apply { inSampleSize = sampleSize }
        )
    }

    private companion object {
        const val MAX_NOTIFICATION_IMAGE_BYTES = 5 * 1024 * 1024
        const val MAX_NOTIFICATION_ICON_PX = 256
    }
}
