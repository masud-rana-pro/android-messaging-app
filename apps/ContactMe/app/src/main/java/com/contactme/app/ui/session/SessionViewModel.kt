package com.contactme.app.ui.session

import androidx.lifecycle.ViewModel
import com.contactme.app.auth.AuthRepository
import com.contactme.app.navigation.AppScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    fun startScreenAfterSplash(): AppScreen {
        return if (authRepository.hasActiveSession()) {
            AppScreen.ProfileSetup
        } else {
            AppScreen.Auth
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        authRepository.signOut()
        onSignedOut()
    }
}
