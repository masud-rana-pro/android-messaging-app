package com.contactme.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
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
import com.contactme.app.ui.screens.GroupCreationScreen
import com.contactme.app.ui.screens.ProfileSetupScreen
import com.contactme.app.ui.screens.SettingsScreen
import com.contactme.app.ui.screens.SplashScreen
import com.contactme.app.ui.screens.StartChatScreen
import com.contactme.app.call.OutgoingCallScreen
import com.contactme.app.call.IncomingCallScreen
import com.contactme.app.call.IncomingCallViewModel
import com.contactme.app.ui.conversation.ConversationViewModel
import com.contactme.app.ui.session.SessionViewModel

@Composable
fun ContactMeApp(
    notificationChatTarget: ChatTarget? = null,
    onNotificationChatTargetConsumed: () -> Unit = {},
    notificationCallId: String? = null,
    onNotificationCallConsumed: () -> Unit = {},
    sessionViewModel: SessionViewModel = hiltViewModel(),
    conversationViewModel: ConversationViewModel = hiltViewModel(),
    presenceViewModel: PresenceViewModel = hiltViewModel(),
    deviceTokenSyncViewModel: DeviceTokenSyncViewModel = hiltViewModel(),
    incomingCallViewModel: IncomingCallViewModel = hiltViewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var currentScreen by remember { mutableStateOf(AppScreen.Splash) }
    val incomingCallState by incomingCallViewModel.uiState.collectAsState()
    val isOpeningChat by conversationViewModel.isOpeningChat.collectAsState()

    var selectedChatTarget by remember {
        mutableStateOf(ChatTarget(title = "ContactMe User", conversationId = null))
    }

    fun openChat(target: ChatTarget) {
        selectedChatTarget = target
        currentScreen = AppScreen.ChatDetail
    }

    fun openOutgoingCall(receiverId: String) {
        selectedChatTarget = ChatTarget(title = "Calling...", conversationId = receiverId)
        currentScreen = AppScreen.OutgoingCall
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

    LaunchedEffect(incomingCallState.activeCall) {
        val call = incomingCallState.activeCall
        if (call != null && currentScreen != AppScreen.IncomingCall) {
            currentScreen = AppScreen.IncomingCall
        }
    }

    LaunchedEffect(notificationChatTarget, currentScreen) {
        val target = notificationChatTarget ?: return@LaunchedEffect
        if (
            currentScreen == AppScreen.Home ||
            currentScreen == AppScreen.ChatDetail ||
            currentScreen == AppScreen.Settings
        ) {
            openNotificationChatIfPossible(target)
            onNotificationChatTargetConsumed()
        }
    }

    LaunchedEffect(notificationCallId) {
        val callId = notificationCallId ?: return@LaunchedEffect
        // The IncomingCallViewModel will likely detect this via Firestore, 
        // but we consume the intent signal here.
        onNotificationCallConsumed()
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    onAuthSuccess = {
                        sessionViewModel.resolveStartScreen { startScreen ->
                            currentScreen = startScreen
                        }
                    }
                )

                AppScreen.ProfileSetup -> ProfileSetupScreen(
                    onProfileReady = { currentScreen = AppScreen.Home }
                )

                AppScreen.Home -> HomeScreen(
                    onConversationSelected = { conversationId, chatName, photoUrl, type ->
                        openChat(
                            ChatTarget(
                                title = chatName,
                                conversationId = conversationId,
                                photoUrl = photoUrl,
                                type = type
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
                    onStartChatSelected = {
                        currentScreen = AppScreen.StartChat
                    },
                    onSettingsSelected = {
                        currentScreen = AppScreen.Settings
                    },
                    onCreateGroupSelected = {
                        currentScreen = AppScreen.CreateGroup
                    }
                )

                AppScreen.CreateGroup -> GroupCreationScreen(
                    onBack = { currentScreen = AppScreen.Home },
                    onGroupCreated = { currentScreen = AppScreen.Home }
                )

                AppScreen.StartChat -> StartChatScreen(
                    onBack = { currentScreen = AppScreen.Home },
                    onUserSelected = { userProfile ->
                        conversationViewModel.openDirectConversation(userProfile) { conversationId, chatName, photoUrl ->
                            openChat(
                                ChatTarget(
                                    title = chatName,
                                    conversationId = conversationId,
                                    photoUrl = photoUrl
                                )
                            )
                        }
                    }
                )

                AppScreen.ChatDetail -> ChatDetailScreen(
                    chatName = selectedChatTarget.title,
                    conversationId = selectedChatTarget.conversationId,
                    chatPhotoUrl = selectedChatTarget.photoUrl,
                    conversationType = selectedChatTarget.type,
                    onBack = { currentScreen = AppScreen.Home },
                    onVoiceCallClick = { receiverId -> openOutgoingCall(receiverId) }
                )

                AppScreen.OutgoingCall -> OutgoingCallScreen(
                    receiverId = selectedChatTarget.conversationId.orEmpty(),
                    onCallEnded = { currentScreen = AppScreen.Home }
                )

                AppScreen.IncomingCall -> IncomingCallScreen(
                    onCallDismissed = {
                        incomingCallViewModel.onCallScreenDismissed()
                        currentScreen = AppScreen.Home
                    },
                    viewModel = incomingCallViewModel
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

        if (isOpeningChat) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Opening chat...", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}
