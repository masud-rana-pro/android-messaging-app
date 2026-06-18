package com.contactme.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.contactme.app.navigation.AppScreen
import com.contactme.app.navigation.ChatTarget
import com.contactme.app.ui.notification.DeviceTokenSyncViewModel
import com.contactme.app.ui.presence.PresenceViewModel
import com.contactme.app.ui.screens.AuthScreen
import com.contactme.app.ui.screens.ChatDetailScreen
import com.contactme.app.ui.screens.HomeScreen
import com.contactme.app.ui.screens.ProfileSetupScreen
import com.contactme.app.ui.screens.SettingsScreen
import com.contactme.app.ui.screens.SplashScreen
import com.contactme.app.ui.conversation.ConversationViewModel
import com.contactme.app.ui.session.SessionViewModel

@Composable
fun ContactMeApp(
    notificationChatTarget: ChatTarget? = null,
    sessionViewModel: SessionViewModel = hiltViewModel(),
    conversationViewModel: ConversationViewModel = hiltViewModel(),
    presenceViewModel: PresenceViewModel = hiltViewModel(),
    deviceTokenSyncViewModel: DeviceTokenSyncViewModel = hiltViewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var currentScreen by remember { mutableStateOf(AppScreen.Splash) }
    var selectedChatTarget by remember {
        mutableStateOf(ChatTarget(title = "ContactMe User", conversationId = null))
    }

    fun openChat(target: ChatTarget) {
        selectedChatTarget = target
        currentScreen = AppScreen.ChatDetail
    }

    fun openNotificationChatIfPossible(target: ChatTarget) {
        selectedChatTarget = target
        currentScreen = AppScreen.ChatDetail
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> presenceViewModel.markOnline()
                Lifecycle.Event.ON_STOP -> presenceViewModel.markOffline()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            presenceViewModel.markOffline()
        }
    }

    LaunchedEffect(currentScreen) {
        presenceViewModel.markOnline()
        deviceTokenSyncViewModel.syncCurrentDevice()
    }

    LaunchedEffect(notificationChatTarget, currentScreen) {
        val target = notificationChatTarget ?: return@LaunchedEffect
        if (
            currentScreen == AppScreen.Home ||
            currentScreen == AppScreen.ChatDetail ||
            currentScreen == AppScreen.Settings
        ) {
            openNotificationChatIfPossible(target)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (currentScreen) {
            AppScreen.Splash -> SplashScreen(
                onSplashFinished = {
                    sessionViewModel.resolveStartScreen { startScreen ->
                        currentScreen = startScreen
                    }
                }
            )

            AppScreen.Auth -> AuthScreen(
                onAuthSuccess = { currentScreen = AppScreen.ProfileSetup }
            )

            AppScreen.ProfileSetup -> ProfileSetupScreen(
                onProfileReady = { currentScreen = AppScreen.Home }
            )

            AppScreen.Home -> HomeScreen(
                onConversationSelected = { conversationId, chatName, photoUrl ->
                    openChat(
                        ChatTarget(
                            title = chatName,
                            conversationId = conversationId,
                            photoUrl = photoUrl
                        )
                    )
                },
                onDiscoveredUserSelected = { userProfile ->
                    conversationViewModel.openDirectConversation(userProfile) { conversationId, chatName, photoUrl ->
                        openChat(
                            ChatTarget(
                                title = chatName,
                                conversationId = conversationId,
                                photoUrl = photoUrl
                            )
                        )
                    }
                },
                onSettingsSelected = {
                    currentScreen = AppScreen.Settings
                }
            )

            AppScreen.ChatDetail -> ChatDetailScreen(
                chatName = selectedChatTarget.title,
                conversationId = selectedChatTarget.conversationId,
                chatPhotoUrl = selectedChatTarget.photoUrl,
                onBack = { currentScreen = AppScreen.Home }
            )

            AppScreen.Settings -> SettingsScreen(
                onBack = { currentScreen = AppScreen.Home },
                onEditProfile = { currentScreen = AppScreen.ProfileSetup },
                onSignOut = {
                    sessionViewModel.signOut {
                        deviceTokenSyncViewModel.resetSyncState()
                        currentScreen = AppScreen.Auth
                    }
                }
            )
        }
    }
}
