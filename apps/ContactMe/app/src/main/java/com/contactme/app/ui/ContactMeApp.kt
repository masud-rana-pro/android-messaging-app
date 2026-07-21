package com.contactme.app.ui

import android.util.Log
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.os.Build
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.contactme.app.call.CallType
import com.contactme.app.call.ActiveCallStore
import com.contactme.app.conversation.ConversationType
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
    var previousScreen by remember { mutableStateOf(AppScreen.Home) }
    val incomingCallState by incomingCallViewModel.uiState.collectAsState()
    val isOpeningChat by conversationViewModel.isOpeningChat.collectAsState()
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            Log.d("ContactMeApp", "Notification permission granted: $isGranted")
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var selectedChatTarget by remember {
        mutableStateOf(ChatTarget(title = "ContactMe User", conversationId = null))
    }
    var currentCallPeerId by remember { mutableStateOf("") }
    var currentCallType by remember { mutableStateOf(CallType.Audio) }
    var shouldStartOutgoingCall by remember { mutableStateOf(true) }

    fun openChat(target: ChatTarget) {
        Log.d("ContactMeApp", "openChat: target=$target")
        selectedChatTarget = target
        previousScreen = currentScreen
        currentScreen = AppScreen.ChatDetail
    }

    fun openOutgoingCall(receiverId: String, type: CallType = CallType.Audio) {
        Log.d("ContactMeApp", "openOutgoingCall: receiverId=$receiverId, type=$type")
        currentCallPeerId = receiverId
        currentCallType = type
        shouldStartOutgoingCall = true
        previousScreen = currentScreen
        currentScreen = AppScreen.OutgoingCall
    }

    fun openStoredCallIfPossible(): Boolean {
        val activeCall = ActiveCallStore.read(context) ?: return false
        return when (activeCall.role) {
            ActiveCallStore.ROLE_OUTGOING -> {
                currentCallPeerId = activeCall.peerId
                currentCallType = activeCall.type
                shouldStartOutgoingCall = false
                currentScreen = AppScreen.OutgoingCall
                true
            }
            ActiveCallStore.ROLE_INCOMING -> {
                incomingCallViewModel.openCallFromNotification(activeCall.callId) {
                    currentScreen = AppScreen.IncomingCall
                }
                true
            }
            else -> false
        }
    }

    fun isCallScreenActive(): Boolean =
        currentScreen == AppScreen.OutgoingCall || currentScreen == AppScreen.IncomingCall

    fun hasStoredActiveCall(): Boolean = ActiveCallStore.read(context) != null

    fun openNotificationChatIfPossible(target: ChatTarget) {
        if (isCallScreenActive() || hasStoredActiveCall()) return
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
        if (call != null && currentScreen != AppScreen.IncomingCall && currentScreen != AppScreen.OutgoingCall) {
            currentScreen = AppScreen.IncomingCall
        }
    }

    LaunchedEffect(notificationChatTarget, currentScreen) {
        val target = notificationChatTarget ?: return@LaunchedEffect
        if (
            !isCallScreenActive() &&
            !hasStoredActiveCall() &&
            (currentScreen == AppScreen.Home ||
            currentScreen == AppScreen.ChatDetail ||
            currentScreen == AppScreen.Settings)
        ) {
            openNotificationChatIfPossible(target)
            onNotificationChatTargetConsumed()
        }
    }

    LaunchedEffect(notificationCallId) {
        val callId = notificationCallId ?: return@LaunchedEffect
        val stored = ActiveCallStore.read(context)
        if (stored?.callId == callId && stored.role == ActiveCallStore.ROLE_OUTGOING) {
            currentCallPeerId = stored.peerId
            currentCallType = stored.type
            shouldStartOutgoingCall = false
            currentScreen = AppScreen.OutgoingCall
            onNotificationCallConsumed()
        } else {
            incomingCallViewModel.openCallFromNotification(callId) {
                previousScreen = currentScreen
                currentScreen = AppScreen.IncomingCall
                onNotificationCallConsumed()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentScreen) {
                AppScreen.Splash -> SplashScreen(
                    onSplashFinished = {
                        if (!openStoredCallIfPossible()) {
                            sessionViewModel.resolveStartScreen { startScreen ->
                                currentScreen = startScreen
                            }
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
                        if (!openStoredCallIfPossible()) {
                            openChat(
                                ChatTarget(
                                    title = chatName,
                                    conversationId = conversationId,
                                    photoUrl = photoUrl,
                                    type = type
                                )
                            )
                        }
                    },
                    onDiscoveredUserSelected = { userProfile ->
                        if (!openStoredCallIfPossible()) {
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
                    },
                    onStartChatSelected = {
                        if (!openStoredCallIfPossible()) currentScreen = AppScreen.StartChat
                    },
                    onSettingsSelected = {
                        if (!openStoredCallIfPossible()) currentScreen = AppScreen.Settings
                    },
                    onCreateGroupSelected = {
                        if (!openStoredCallIfPossible()) currentScreen = AppScreen.CreateGroup
                    },
                    onCallSelected = { receiverId, type -> openOutgoingCall(receiverId, type) }
                )

                AppScreen.CreateGroup -> GroupCreationScreen(
                    onBack = { if (!openStoredCallIfPossible()) currentScreen = AppScreen.Home },
                    onGroupCreated = { conversationId, title ->
                        openChat(
                            ChatTarget(
                                title = title,
                                conversationId = conversationId,
                                type = ConversationType.Group
                            )
                        )
                    }
                )

                AppScreen.StartChat -> StartChatScreen(
                    onBack = { if (!openStoredCallIfPossible()) currentScreen = AppScreen.Home },
                    onAudioCall = { userProfile -> openOutgoingCall(userProfile.userId, CallType.Audio) },
                    onVideoCall = { userProfile -> openOutgoingCall(userProfile.userId, CallType.Video) },
                    onUserSelected = { userProfile ->
                        Log.d("ContactMeApp", "StartChat: onUserSelected=${userProfile.userId}")
                        conversationViewModel.openDirectConversation(userProfile) { conversationId, chatName, photoUrl ->
                            Log.d("ContactMeApp", "StartChat: conversation ready: $conversationId. Navigating...")
                            try {
                                openChat(
                                    ChatTarget(
                                        title = chatName,
                                        conversationId = conversationId,
                                        photoUrl = photoUrl
                                    )
                                )
                            } catch (e: Exception) {
                                Log.e("ContactMeApp", "Navigation to ChatDetail failed", e)
                            }
                        }
                    },
                    conversationViewModel = conversationViewModel
                )

                AppScreen.ChatDetail -> ChatDetailScreen(
                    chatName = selectedChatTarget.title,
                    conversationId = selectedChatTarget.conversationId,
                    chatPhotoUrl = selectedChatTarget.photoUrl,
                    conversationType = selectedChatTarget.type,
                    onBack = { if (!openStoredCallIfPossible()) currentScreen = AppScreen.Home },
                    onVoiceCallClick = { receiverId -> openOutgoingCall(receiverId, CallType.Audio) },
                    onVideoCallClick = { receiverId -> openOutgoingCall(receiverId, CallType.Video) }
                )

                AppScreen.OutgoingCall -> OutgoingCallScreen(
                    receiverId = currentCallPeerId,
                    callType = currentCallType,
                    startNewCall = shouldStartOutgoingCall,
                    onCallEnded = {
                        shouldStartOutgoingCall = true
                        currentScreen = if (previousScreen == AppScreen.OutgoingCall || previousScreen == AppScreen.IncomingCall) {
                            AppScreen.Home
                        } else {
                            previousScreen
                        }
                    }
                )

                AppScreen.IncomingCall -> IncomingCallScreen(
                    onCallDismissed = {
                        incomingCallViewModel.onCallScreenDismissed()
                        currentScreen = previousScreen
                    },
                    viewModel = incomingCallViewModel
                )

                AppScreen.Settings -> SettingsScreen(
                    onBack = { if (!openStoredCallIfPossible()) currentScreen = AppScreen.Home },
                    onEditProfile = { if (!openStoredCallIfPossible()) currentScreen = AppScreen.ProfileSetup },
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
