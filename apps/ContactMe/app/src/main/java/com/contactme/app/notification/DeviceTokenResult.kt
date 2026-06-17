package com.contactme.app.notification

sealed interface DeviceTokenResult {
    data object Success : DeviceTokenResult
    data class Error(val message: String) : DeviceTokenResult
}
