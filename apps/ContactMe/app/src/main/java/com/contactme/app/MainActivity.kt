package com.contactme.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.contactme.app.navigation.ChatTarget
import com.contactme.app.navigation.NotificationNavigation
import com.contactme.app.ui.ContactMeApp
import com.contactme.app.ui.theme.ContactMeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var notificationChatTarget by mutableStateOf<ChatTarget?>(null)
    private var notificationCallId by mutableStateOf<String?>(null)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // The app can still run without notification permission.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationChatTarget = NotificationNavigation.chatTargetFrom(intent)
        notificationCallId = NotificationNavigation.callIdFrom(intent)
        requestNotificationPermissionIfNeeded()
        setContent {
            ContactMeTheme {
                ContactMeApp(
                    notificationChatTarget = notificationChatTarget,
                    onNotificationChatTargetConsumed = ::consumeNotificationChatTarget,
                    notificationCallId = notificationCallId,
                    onNotificationCallConsumed = ::consumeNotificationCall
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationChatTarget = NotificationNavigation.chatTargetFrom(intent)
        notificationCallId = NotificationNavigation.callIdFrom(intent)
    }

    private fun consumeNotificationChatTarget() {
        notificationChatTarget = null
        NotificationNavigation.clearChatTarget(intent)
    }

    private fun consumeNotificationCall() {
        notificationCallId = null
        NotificationNavigation.clearChatTarget(intent)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
