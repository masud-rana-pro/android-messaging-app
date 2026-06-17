package com.contactme.app.notification

interface DeviceTokenRepository {
    suspend fun syncCurrentDeviceToken(userId: String): DeviceTokenResult

    suspend fun syncRefreshedToken(token: String): DeviceTokenResult
}
