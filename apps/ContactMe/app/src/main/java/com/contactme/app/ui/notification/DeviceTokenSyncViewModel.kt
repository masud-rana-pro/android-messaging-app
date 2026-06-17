package com.contactme.app.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contactme.app.auth.AuthRepository
import com.contactme.app.notification.DeviceTokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class DeviceTokenSyncViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val deviceTokenRepository: DeviceTokenRepository
) : ViewModel() {
    private var lastSyncedUserId: String? = null

    fun syncCurrentDevice() {
        val userId = authRepository.currentUserId() ?: return

        if (lastSyncedUserId == userId) return

        lastSyncedUserId = userId
        viewModelScope.launch {
            deviceTokenRepository.syncCurrentDeviceToken(userId)
        }
    }
}
