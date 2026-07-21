package com.contactme.app.call

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.contactme.app.R
import com.contactme.app.MainActivity
import com.contactme.app.navigation.NotificationNavigation
import com.contactme.app.notification.ContactMeNotificationChannels

class CallForegroundService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        val callId = intent?.getStringExtra(EXTRA_CALL_ID) ?: "Unknown"
        val isVideoCall = intent?.getBooleanExtra(EXTRA_IS_VIDEO_CALL, false) == true
        runCatching { startForegroundServiceInternal(callId, isVideoCall) }
            .onFailure {
                Log.e(TAG, "Could not promote call service to foreground", it)
                stopSelf()
            }
        
        return START_NOT_STICKY
    }

    private fun startForegroundServiceInternal(callId: String, isVideoCall: Boolean) {
        val openCallIntent = PendingIntent.getActivity(
            this,
            callId.hashCode() and Int.MAX_VALUE,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(NotificationNavigation.EXTRA_CALL_ID, callId)
                putExtra(NotificationNavigation.EXTRA_NOTIFICATION_TYPE, "active_call")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, ContactMeNotificationChannels.CALLS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(if (isVideoCall) "ContactMe Video Call" else "ContactMe Audio Call")
            .setContentText("Call in progress...")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setContentIntent(openCallIntent)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val serviceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or
                if (isVideoCall) ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA else 0
            startForeground(
                NOTIFICATION_ID,
                notification,
                serviceType
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val EXTRA_CALL_ID = "call_id"
        private const val EXTRA_IS_VIDEO_CALL = "is_video_call"
        private const val EXTRA_ROLE = "role"
        private const val EXTRA_PEER_ID = "peer_id"
        private const val ACTION_STOP = "STOP_SERVICE"
        private const val TAG = "CallForegroundService"

        fun start(
            context: Context,
            callId: String,
            callType: CallType,
            role: String = "",
            peerId: String = ""
        ) {
            if (role.isNotBlank()) {
                ActiveCallStore.save(context, callId, role, peerId, callType)
            }
            val intent = Intent(context, CallForegroundService::class.java).apply {
                putExtra(EXTRA_CALL_ID, callId)
                putExtra(EXTRA_IS_VIDEO_CALL, callType == CallType.Video)
                putExtra(EXTRA_ROLE, role)
                putExtra(EXTRA_PEER_ID, peerId)
            }
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }.onFailure { Log.e(TAG, "Could not start call foreground service", it) }
        }

        fun stop(context: Context) {
            val intent = Intent(context, CallForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            runCatching { context.startService(intent) }
                .onFailure { Log.e(TAG, "Could not stop call foreground service", it) }
        }
    }
}
