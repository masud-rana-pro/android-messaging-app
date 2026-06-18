package com.contactme.app.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contactme.app.auth.AuthRepository
import com.contactme.app.navigation.AppScreen
import com.contactme.app.notification.DeviceTokenRepository
import com.contactme.app.profile.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val deviceTokenRepository: DeviceTokenRepository
) : ViewModel() {
    fun resolveStartScreen(onResolved: (AppScreen) -> Unit) {
        viewModelScope.launch {
            val userId = authRepository.currentUserId()

            val startScreen = when {
                userId == null -> AppScreen.Auth
                profileRepository.isProfileComplete(userId) -> AppScreen.Home
                else -> AppScreen.ProfileSetup
            }

            onResolved(startScreen)
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        val userId = authRepository.currentUserId()
        viewModelScope.launch {
            if (userId != null) {
                deviceTokenRepository.removeCurrentDeviceToken(userId)
            }
            authRepository.signOut()
            onSignedOut()
        }
    }
}
