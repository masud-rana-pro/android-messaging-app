package com.contactme.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object ContactMeNotificationChannels {
    const val MESSAGES_CHANNEL_ID = "messages"
    const val CALLS_CHANNEL_ID = "calls"
    const val SYSTEM_CHANNEL_ID = "system"

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannels(
            listOf(
                NotificationChannel(
                    MESSAGES_CHANNEL_ID,
                    "Messages",
                    NotificationManager.IMPORTANCE_HIGH
                ),
                NotificationChannel(
                    CALLS_CHANNEL_ID,
                    "Calls",
                    NotificationManager.IMPORTANCE_HIGH
                ),
                NotificationChannel(
                    SYSTEM_CHANNEL_ID,
                    "System",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        )
    }
}
