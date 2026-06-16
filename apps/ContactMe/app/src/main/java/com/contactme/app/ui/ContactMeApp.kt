package com.contactme.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.contactme.app.navigation.AppScreen
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
    sessionViewModel: SessionViewModel = hiltViewModel(),
    conversationViewModel: ConversationViewModel = hiltViewModel()
) {
    var currentScreen by remember { mutableStateOf(AppScreen.Splash) }
    var selectedChatName by remember { mutableStateOf("Ayesha Rahman") }
    var selectedConversationId by remember { mutableStateOf<String?>(null) }

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
                onChatSelected = { chatName ->
                    selectedChatName = chatName
                    selectedConversationId = null
                    currentScreen = AppScreen.ChatDetail
                },
                onDiscoveredUserSelected = { userProfile ->
                    conversationViewModel.openDirectConversation(userProfile) { conversationId, chatName ->
                        selectedConversationId = conversationId
                        selectedChatName = chatName
                        currentScreen = AppScreen.ChatDetail
                    }
                },
                onSettingsSelected = {
                    currentScreen = AppScreen.Settings
                }
            )

            AppScreen.ChatDetail -> ChatDetailScreen(
                chatName = selectedChatName,
                conversationId = selectedConversationId,
                onBack = { currentScreen = AppScreen.Home }
            )

            AppScreen.Settings -> SettingsScreen(
                onBack = { currentScreen = AppScreen.Home },
                onEditProfile = { currentScreen = AppScreen.ProfileSetup },
                onSignOut = {
                    sessionViewModel.signOut {
                        currentScreen = AppScreen.Auth
                    }
                }
            )
        }
    }
}
