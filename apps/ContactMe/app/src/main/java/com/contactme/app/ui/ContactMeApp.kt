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
import com.contactme.app.navigation.AppScreen
import com.contactme.app.ui.screens.AuthScreen
import com.contactme.app.ui.screens.HomeScreen
import com.contactme.app.ui.screens.ProfileSetupScreen
import com.contactme.app.ui.screens.SplashScreen

@Composable
fun ContactMeApp() {
    var currentScreen by remember { mutableStateOf(AppScreen.Splash) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (currentScreen) {
            AppScreen.Splash -> SplashScreen(
                onSplashFinished = { currentScreen = AppScreen.Auth }
            )

            AppScreen.Auth -> AuthScreen(
                onAuthSuccess = { currentScreen = AppScreen.ProfileSetup }
            )

            AppScreen.ProfileSetup -> ProfileSetupScreen(
                onProfileReady = { currentScreen = AppScreen.Home }
            )

            AppScreen.Home -> HomeScreen()
        }
    }
}
